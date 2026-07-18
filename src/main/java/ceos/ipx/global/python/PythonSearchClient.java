package ceos.ipx.global.python;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.request.search.PythonAddManualRequest;
import ceos.ipx.global.python.dto.request.search.PythonSearchRequest;
import ceos.ipx.global.python.dto.response.search.PythonAddManualResponse;
import ceos.ipx.global.python.dto.response.search.PythonSearchResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
 * Python 검색/분석 서버 통신 클라이언트.
 *
 * 엔드포인트:
 *   - POST /search              : 검색 실행 (동기 블로킹, @Async 스레드에서 호출)
 *   - POST /search/add-manual   : 수동 특허 추가
 *
 * 예외 처리 원칙:
 *   - 4xx/5xx: 상태 코드별 매핑 (handleError)
 *   - 타임아웃/연결 실패: PYTHON_SERVER_TIMEOUT
 *   - 응답 null: PYTHON_SERVER_ERROR (호출자는 null 걱정 없음)
 *   - 그 외: PYTHON_SERVER_ERROR
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonSearchClient {

    private final WebClient pythonWebClient;

    // ============================================================
    // 검색 실행
    // ============================================================

    public PythonSearchResultResponse executeSearch(PythonSearchRequest request) {
        log.info("[Python] 검색 요청 시작: searchId={}, title={}",
                request.searchId(), request.title());

        PythonSearchResultResponse response;
        try {
            response = pythonWebClient.post()
                    .uri("/search")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonSearchResultResponse.class)
                    .block(Duration.ofSeconds(120));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e, "검색 요청 실패: searchId=" + request.searchId());
        }

        if (response == null) {
            log.error("[Python] 검색 응답 null: searchId={}", request.searchId());
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[Python] 검색 완료: searchId={}, resultCount={}",
                request.searchId(),
                response.results() != null ? response.results().size() : 0);
        return response;
    }

    // ============================================================
    // 수동 특허 추가
    // ============================================================

    public PythonAddManualResponse addManual(PythonAddManualRequest request) {
        log.info("[Python] 수동 추가 요청 시작: count={}", request.applicationNumbers().size());

        PythonAddManualResponse response;
        try {
            response = pythonWebClient.post()
                    .uri("/search/add-manual")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(PythonAddManualResponse.class)
                    .block(Duration.ofSeconds(90));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e, "수동 추가 실패");
        }

        if (response == null) {
            log.error("[Python] 수동 추가 응답 null");
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[Python] 수동 추가 완료: newResults={}",
                response.results() != null ? response.results().size() : 0);
        return response;
    }

    // ============================================================
    // 예외 매핑
    // ============================================================

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode status = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("(empty)")
                .flatMap(body -> {
                    log.error("[Python] 오류 응답: status={}, body={}", status, body);

                    if (HttpStatus.NOT_FOUND.equals(status)) {
                        return Mono.error(new BusinessException(ErrorCode.SEARCH_NOT_FOUND));
                    }

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