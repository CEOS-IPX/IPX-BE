package ceos.ipx.global.python;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.request.search.PythonComponentExtractRequest;
import ceos.ipx.global.python.dto.response.search.PythonComponentExtractResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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

    /**
     * Python 서버의 에러 응답을 Spring 예외로 매핑
     *
     * Python 응답 예시
     *   400 + {"detail": {"error": "INVALID_INVENTION"}} → INVALID_INVENTION
     *   400 + 그 외 → INVALID_INPUT
     *   502 → PYTHON_SERVER_ERROR (LLM 응답 처리 실패)
     *   500 → PYTHON_SERVER_ERROR (Python 서버 내부 오류)
     */
    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode status = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("(empty)")
                .flatMap(body -> {
                    // 400: 사용자 입력 문제 (특정 케이스 파싱)
                    if (status.is4xxClientError()) {
                        BusinessException specificException = tryParseInvalidInvention(body);
                        if (specificException != null) {
                            log.warn("[Python] Invalid invention: body={}", body);
                            return Mono.error(specificException);
                        }
                        log.error("[Python] 4xx 오류 (파싱 불가): status={}, body={}", status, body);
                        return Mono.error(new BusinessException(ErrorCode.PYTHON_SERVER_ERROR));
                    }

                    // 5xx: 시스템 오류
                    log.error("[Python] 5xx 오류: status={}, body={}", status, body);
                    return Mono.error(new BusinessException(ErrorCode.PYTHON_SERVER_ERROR));
                });
    }

    /**
     * Python 400 응답에서 INVALID_INVENTION 케이스 파싱
     *
     * Python 응답 구조: {"detail": {"error": "INVALID_INVENTION"}}
     *
     * return 파싱 성공 시 INVALID_INVENTION 예외, 실패 시 null
     */
    private BusinessException tryParseInvalidInvention(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode detail = root.get("detail");

            if (detail == null || !detail.isObject()) {
                return null;
            }

            JsonNode errorNode = detail.get("error");
            if (errorNode == null || !"INVALID_INVENTION".equals(errorNode.asText())) {
                return null;
            }

            JsonNode reasonNode = detail.get("reason");
            String reason = reasonNode != null ? reasonNode.asText() :
                    "특허 등록 가능한 기술적 특징 부족";

            return new BusinessException(ErrorCode.INVALID_INVENTION);

        } catch (Exception e) {
            // 파싱 실패는 예상 가능 (다른 형식의 400 응답일 수 있음)
            return null;
        }
    }

    private BusinessException mapException(Exception e, String context) {
        if (e instanceof TimeoutException
                || (e.getCause() != null && e.getCause() instanceof TimeoutException)) {
            log.error("[Python] 타임아웃: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_TIMEOUT);
        }

        if (e instanceof WebClientRequestException) {
            log.error("[Python] 연결 실패: {}", context, e);
            return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.error("[Python] 예외: {}", context, e);
        return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
    }
}