package ceos.ipx.global.python;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.request.novelty.PythonNoveltyRequest;
import ceos.ipx.global.python.dto.response.novelty.PythonNoveltyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Python 신규성 분석 API 클라이언트
 *
 * 엔드포인트:
 *   - POST /analyze/novelty : 신규성 분석 (Python이 상위 3건 병렬 LLM 분석)
 *
 * Python 내부에서 LLM 병렬 처리하므로 Spring은 동기 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonNoveltyClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(90);
    private final WebClient pythonWebClient;

    public PythonNoveltyResponse analyzeNovelty(PythonNoveltyRequest request) {
        log.info("[Python][Novelty] 신규성 분석 요청: components={}, priorArts={}",
                request.components() != null ? request.components().size() : 0,
                request.priorArts() != null ? request.priorArts().size() : 0);

        PythonNoveltyResponse response;
        try {
            response = pythonWebClient.post()
                    .uri("/analyze/novelty")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonNoveltyResponse.class)
                    .block(TIMEOUT);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e, "신규성 분석 실패");
        }

        if (response == null) {
            log.error("[Python][Novelty] 응답 null");
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[Python][Novelty] 완료: d1={}, similarity={}",
                response.d1ApplicationNumber(), response.overallSimilarity());
        return response;
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode status = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("(empty)")
                .flatMap(body -> {
                    log.error("[Python][Novelty] 오류 응답: status={}, body={}", status, body);
                    return Mono.error(new BusinessException(ErrorCode.PYTHON_SERVER_ERROR));
                });
    }

    private BusinessException mapException(Exception e, String context) {
        if (e instanceof TimeoutException
                || (e.getCause() != null && e.getCause() instanceof TimeoutException)) {
            log.error("[Python][Novelty] 타임아웃: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_TIMEOUT);
        }

        if (e instanceof WebClientRequestException) {
            log.error("[Python][Novelty] 연결 실패: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.error("[Python][Novelty] 예외: {}", context, e);
        return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
    }
}