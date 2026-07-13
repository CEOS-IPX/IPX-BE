package ceos.ipx.global.python;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.request.search.PythonComponentExtractRequest;
import ceos.ipx.global.python.dto.response.search.PythonComponentExtractResponse;
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
 * Python 구성요소 추출 API 클라이언트.
 *
 * 지원 엔드포인트:
 *   - POST /components/extract : AI 자동 구성요소 추출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonComponentClient {

    private final WebClient pythonWebClient;

    public PythonComponentExtractResponse extract(PythonComponentExtractRequest request) {
        log.info("[Python] 구성요소 추출 요청: title={}", request.title());

        PythonComponentExtractResponse response;
        try {
            response = pythonWebClient.post()
                    .uri("/components/extract")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonComponentExtractResponse.class)
                    .block(Duration.ofSeconds(60));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e, "구성요소 추출 실패");
        }

        if (response == null) {
            log.error("[Python] 구성요소 추출 응답 null");
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[Python] 구성요소 추출 완료: count={}",
                response.components() != null ? response.components().size() : 0);
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
                    log.error("[Python] 오류 응답: status={}, body={}", status, body);
                    return Mono.error(new BusinessException(ErrorCode.PYTHON_SERVER_ERROR));
                });
    }

    private BusinessException mapException(Exception e, String context) {
        if (e instanceof TimeoutException
                || (e.getCause() != null && e.getCause() instanceof TimeoutException)) {
            log.error("[Python] 타임아웃: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_TIMEOUT);
        }

        if (e instanceof WebClientRequestException) {
            log.error("[Python] 연결 실패: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_TIMEOUT);
        }

        if (e instanceof WebClientResponseException webEx) {
            log.error("[Python] 오류 응답: {}, status={}", context, webEx.getStatusCode(), e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.error("[Python] 예외: {}", context, e);
        return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
    }
}