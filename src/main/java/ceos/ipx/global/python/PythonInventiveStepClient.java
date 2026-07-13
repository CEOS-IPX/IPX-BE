package ceos.ipx.global.python;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.request.inventivestep.*;
import ceos.ipx.global.python.dto.response.inventivestep.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Python 진보성 분석 API 클라이언트
 *
 * 엔드포인트:
 *   - POST /analyze/inventive-step/select-secondary
 *   - POST /analyze/inventive-step/select-categories
 *   - POST /analyze/inventive-step/generate/numerical-limit
 *   - POST /analyze/inventive-step/generate/combination-motivation
 *   - POST /analyze/inventive-step/generate/common-technique
 *   - POST /analyze/inventive-step/generate/simple-design
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonInventiveStepClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final WebClient pythonWebClient;

    // ============================================================
    // D2 자동 선정
    // ============================================================

    public PythonSelectSecondaryResponse selectSecondary(PythonSelectSecondaryRequest request) {
        log.info("[Python][InventiveStep] D2 선정 요청: candidates={}",
                request.candidates() != null ? request.candidates().size() : 0);

        PythonSelectSecondaryResponse response;
        try {
            response = pythonWebClient.post()
                    .uri("/analyze/inventive-step/select-secondary")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonSelectSecondaryResponse.class)
                    .block(TIMEOUT);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e, "D2 선정 실패");
        }

        if (response == null) {
            log.error("[Python][InventiveStep] D2 선정 응답 null");
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[Python][InventiveStep] D2 선정 완료: d2={}", response.d2ApplicationNumber());
        return response;
    }

    // ============================================================
    // 카테고리 자동 선정
    // ============================================================

    public PythonSelectCategoriesResponse selectCategories(PythonSelectCategoriesRequest request) {
        log.info("[Python][InventiveStep] 카테고리 선정 요청");

        PythonSelectCategoriesResponse response;
        try {
            response = pythonWebClient.post()
                    .uri("/analyze/inventive-step/select-categories")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonSelectCategoriesResponse.class)
                    .block(TIMEOUT);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e, "카테고리 선정 실패");
        }

        if (response == null) {
            log.error("[Python][InventiveStep] 카테고리 선정 응답 null");
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[Python][InventiveStep] 카테고리 선정 완료: categories={}", response.categories());
        return response;
    }

    // ============================================================
    // 4개 카테고리 논리 생성
    // ============================================================

    public PythonNumericalLimitResult generateNumericalLimit(PythonNumericalLimitRequest request) {
        return callGenerate(
                "/analyze/inventive-step/generate/numerical-limit",
                request,
                PythonNumericalLimitResult.class,
                "수치한정"
        );
    }

    public PythonCombinationMotivationResult generateCombinationMotivation(
            PythonCombinationMotivationRequest request) {
        return callGenerate(
                "/analyze/inventive-step/generate/combination-motivation",
                request,
                PythonCombinationMotivationResult.class,
                "복수인용발명결합"
        );
    }

    public PythonCommonTechniqueResult generateCommonTechnique(PythonCommonTechniqueRequest request) {
        return callGenerate(
                "/analyze/inventive-step/generate/common-technique",
                request,
                PythonCommonTechniqueResult.class,
                "주지관용기술"
        );
    }

    public PythonSimpleDesignResult generateSimpleDesign(PythonSimpleDesignRequest request) {
        return callGenerate(
                "/analyze/inventive-step/generate/simple-design",
                request,
                PythonSimpleDesignResult.class,
                "단순설계변경"
        );
    }

    // ============================================================
    // 공통 호출 로직
    // ============================================================

    private <TReq, TRes> TRes callGenerate(String uri, TReq request, Class<TRes> responseType, String label) {
        log.info("[Python][InventiveStep] {} 생성 요청", label);

        TRes response;
        try {
            response = pythonWebClient.post()
                    .uri(uri)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(responseType)
                    .block(TIMEOUT);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e, label + " 생성 실패");
        }

        if (response == null) {
            log.error("[Python][InventiveStep] {} 응답 null", label);
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[Python][InventiveStep] {} 생성 완료", label);
        return response;
    }

    // ============================================================
    // 예외 매핑
    // ============================================================

    private Mono<? extends Throwable> handleError(
            org.springframework.web.reactive.function.client.ClientResponse response) {
        HttpStatusCode status = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("(empty)")
                .flatMap(body -> {
                    log.error("[Python][InventiveStep] 오류 응답: status={}, body={}", status, body);
                    return Mono.error(new BusinessException(ErrorCode.PYTHON_SERVER_ERROR));
                });
    }

    private BusinessException mapException(Exception e, String context) {
        if (e instanceof TimeoutException
                || (e.getCause() != null && e.getCause() instanceof TimeoutException)) {
            log.error("[Python][InventiveStep] 타임아웃: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_TIMEOUT);
        }

        if (e instanceof WebClientRequestException) {
            log.error("[Python][InventiveStep] 연결 실패: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_TIMEOUT);
        }

        if (e instanceof WebClientResponseException webEx) {
            log.error("[Python][InventiveStep] 오류 응답: {}, status={}",
                    context, webEx.getStatusCode(), e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.error("[Python][InventiveStep] 예외: {}", context, e);
        return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
    }
}