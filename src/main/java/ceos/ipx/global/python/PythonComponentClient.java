package ceos.ipx.global.python;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.request.PythonComponentExtractRequest;
import ceos.ipx.global.python.dto.response.PythonComponentExtractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Python 구성요소 추출 API 클라이언트
 *
 * 엔드포인트
 *   - POST /components/extract : AI 자동 구성요소 추출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonComponentClient {

    private final WebClient pythonWebClient;

    /**
     * AI 자동 구성요소 추출
     */
    public PythonComponentExtractResponse extract(PythonComponentExtractRequest request) {
        log.info("[Python] 구성요소 추출 요청: title={}", request.title());

        try {
            PythonComponentExtractResponse response = pythonWebClient.post()
                    .uri("/components/extract")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonComponentExtractResponse.class)
                    .block(Duration.ofSeconds(60));

            log.info("[Python] 구성요소 추출 완료: count={}",
                    response != null && response.components() != null ? response.components().size() : 0);
            return response;

        } catch (WebClientResponseException e) {
            log.error("[Python] 구성요소 추출 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        } catch (Exception e) {
            log.error("[Python] 구성요소 추출 예외", e);
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }
    }

    private Mono<? extends Throwable> handleError(
            org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("(empty)")
                .flatMap(body -> {
                    log.error("[Python] 오류 응답: status={}, body={}",
                            response.statusCode(), body);
                    return Mono.error(new BusinessException(ErrorCode.PYTHON_SERVER_ERROR));
                });
    }
}