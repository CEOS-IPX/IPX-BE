package ceos.ipx.global.python;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.request.PythonSearchRequest;
import ceos.ipx.global.python.dto.response.PythonCancelResponse;
import ceos.ipx.global.python.dto.response.PythonSearchResultResponse;
import ceos.ipx.global.python.dto.response.PythonSearchStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Python 검색/분석 서버 통신 클라이언트
 *
 * 엔드포인트:
 *   - POST /search              : 검색 실행 (동기 블로킹, @Async 스레드에서 호출)
 *   - GET  /search/{id}/status  : 진행 상태 조회
 *   - POST /search/{id}/cancel  : 검색 중단
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonSearchClient {

    private final WebClient pythonWebClient;

    /**
     * 검색 실행
     * Python이 완료된 결과를 반환할 때까지 블로킹
     * 호출자는 반드시 @Async 스레드에서 실행할 것
     */
    public PythonSearchResultResponse executeSearch(PythonSearchRequest request) {
        log.info("[Python] 검색 요청 시작: searchId={}, title={}",
                request.searchId(), request.title());

        try {
            PythonSearchResultResponse response = pythonWebClient.post()
                    .uri("/search")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonSearchResultResponse.class)
                    .block(Duration.ofSeconds(120));

            log.info("[Python] 검색 완료: searchId={}, resultCount={}",
                    request.searchId(),
                    response != null && response.results() != null ? response.results().size() : 0);
            return response;

        } catch (WebClientResponseException e) {
            log.error("[Python] 검색 요청 실패: searchId={}, status={}, body={}",
                    request.searchId(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        } catch (Exception e) {
            log.error("[Python] 검색 요청 예외: searchId={}", request.searchId(), e);
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }
    }

    /**
     * 검색 진행 상태 조회 (폴링용)
     */
    public PythonSearchStatusResponse getStatus(String searchId) {
        try {
            return pythonWebClient.get()
                    .uri("/search/{searchId}/status", searchId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonSearchStatusResponse.class)
                    .block(Duration.ofSeconds(10));

        } catch (WebClientResponseException e) {
            log.warn("[Python] 상태 조회 실패: searchId={}, status={}", searchId, e.getStatusCode());
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }
    }

    /**
     * 검색 중단 요청
     * @return 취소 결과 (이미 완료 상태였으면 cancelled=false)
     */
    public PythonCancelResponse cancel(String searchId) {
        try {
            PythonCancelResponse response = pythonWebClient.post()
                    .uri("/search/{searchId}/cancel", searchId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonCancelResponse.class)
                    .block(Duration.ofSeconds(10));

            log.info("[Python] 검색 중단 요청 완료: searchId={}, cancelled={}",
                    searchId, response != null ? response.cancelled() : null);
            return response;

        } catch (WebClientResponseException e) {
            log.warn("[Python] 검색 중단 실패: searchId={}, status={}", searchId, e.getStatusCode());
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