package ceos.ipx.global.opensearch;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.opensearch.dto.PatentDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenSearch 클라이언트 (WebClient 기반)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenSearchClient {

    private final WebClient opensearchWebClient;
    private final ObjectMapper objectMapper;

    @Value("${opensearch.index:patents}")
    private String indexName;

    /**
     * 여러 출원번호로 특허 문서를 mget으로 일괄 조회
     *
     * 요청 형식: POST /{index}/_mget  body: {"ids": ["...", "..."]}
     * 응답 형식: {"docs": [{"_id": "...", "found": true, "_source": {...}}, ...]}
     */
    public List<PatentDocument> mgetByApplicationNumbers(List<String> applicationNumbers) {
        if (applicationNumbers == null || applicationNumbers.isEmpty()) {
            return List.of();
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ids", applicationNumbers);

        log.info("[OpenSearch] mget 요청: index={}, count={}", indexName, applicationNumbers.size());

        JsonNode responseNode;
        try {
            responseNode = opensearchWebClient.post()
                    .uri("/{index}/_mget", indexName)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(30));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[OpenSearch] mget 실패", e);
            throw new BusinessException(ErrorCode.OPENSEARCH_ERROR);
        }

        if (responseNode == null) {
            log.error("[OpenSearch] mget 응답 null");
            throw new BusinessException(ErrorCode.OPENSEARCH_ERROR);
        }

        return extractDocuments(responseNode);
    }

    /**
     * 단일 출원번호로 조회 (편의 메서드)
     * 찾지 못하면 null 반환
     */
    public PatentDocument getByApplicationNumber(String applicationNumber) {
        List<PatentDocument> docs = mgetByApplicationNumbers(List.of(applicationNumber));
        return docs.isEmpty() ? null : docs.get(0);
    }

    // ============================================================
    // 응답 파싱
    // ============================================================

    /**
     * mget 응답의 docs 배열에서 found=true인 것만 PatentDocument로 변환
     */
    private List<PatentDocument> extractDocuments(JsonNode responseNode) {
        JsonNode docsNode = responseNode.get("docs");
        if (docsNode == null || !docsNode.isArray()) {
            log.warn("[OpenSearch] docs 배열 없음");
            return List.of();
        }

        List<PatentDocument> results = new ArrayList<>();
        for (JsonNode docNode : docsNode) {
            boolean found = docNode.path("found").asBoolean(false);
            if (!found) {
                String missingId = docNode.path("_id").asText();
                log.warn("[OpenSearch] 문서 없음: {}", missingId);
                continue;
            }

            JsonNode sourceNode = docNode.get("_source");
            if (sourceNode == null) {
                continue;
            }

            try {
                PatentDocument doc = objectMapper.treeToValue(sourceNode, PatentDocument.class);
                results.add(doc);
            } catch (Exception e) {
                log.error("[OpenSearch] 문서 파싱 실패: id={}", docNode.path("_id").asText(), e);
            }
        }

        log.info("[OpenSearch] mget 성공: 요청={}, 응답={}", docsNode.size(), results.size());
        return results;
    }

    private Mono<? extends Throwable> handleError(
            org.springframework.web.reactive.function.client.ClientResponse response) {
        HttpStatusCode status = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("(empty)")
                .flatMap(body -> {
                    log.error("[OpenSearch] 오류 응답: status={}, body={}", status, body);
                    return Mono.error(new BusinessException(ErrorCode.OPENSEARCH_ERROR));
                });
    }
}
