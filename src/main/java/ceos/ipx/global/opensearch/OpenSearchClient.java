package ceos.ipx.global.opensearch;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.opensearch.dto.PatentDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 *
 * 지원 기능:
 *   - mget: 여러 특허 문서를 출원번호(_id)로 일괄 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenSearchClient {

    private final WebClient opensearchWebClient;

    @Value("${opensearch.index:patents}")
    private String indexName;

    /**
     * 여러 출원번호로 특허 문서를 mget으로 일괄 조회
     *
     * 요청 형식: POST /{index}/_mget  body: {"ids": ["...", "..."]}
     * 응답 형식: {"docs": [{"_id": "...", "found": true, "_source": {...}}, ...]}
     *
     * 찾지 못한 문서는 결과에서 제외 (found=false)
     */
    public List<PatentDocument> mgetByApplicationNumbers(List<String> applicationNumbers) {
        if (applicationNumbers == null || applicationNumbers.isEmpty()) {
            return List.of();
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ids", applicationNumbers);

        log.info("[OpenSearch] mget 요청: index={}, count={}", indexName, applicationNumbers.size());

        MgetResponse response;
        try {
            response = opensearchWebClient.post()
                    .uri("/{index}/_mget", indexName)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(MgetResponse.class)
                    .block(Duration.ofSeconds(30));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[OpenSearch] mget 실패", e);
            throw new BusinessException(ErrorCode.OPENSEARCH_ERROR);
        }

        if (response == null || response.docs() == null) {
            log.error("[OpenSearch] mget 응답 null");
            throw new BusinessException(ErrorCode.OPENSEARCH_ERROR);
        }

        return extractFoundDocuments(response.docs());
    }

    /**
     * 단일 출원번호로 조회
     * 찾지 못하면 null 반환
     */
    public PatentDocument getByApplicationNumber(String applicationNumber) {
        List<PatentDocument> docs = mgetByApplicationNumbers(List.of(applicationNumber));
        return docs.isEmpty() ? null : docs.getFirst();
    }

    // ============================================================
    // 응답 파싱
    // ============================================================

    /**
     * mget docs 중 found=true인 것만 PatentDocument로 추출
     */
    private List<PatentDocument> extractFoundDocuments(List<MgetDoc> docs) {
        List<PatentDocument> results = new ArrayList<>();
        for (MgetDoc doc : docs) {
            if (!Boolean.TRUE.equals(doc.found())) {
                log.warn("[OpenSearch] 문서 없음: {}", doc.id());
                continue;
            }

            if (doc.source() == null) {
                log.warn("[OpenSearch] _source 없음: {}", doc.id());
                continue;
            }

            results.add(doc.source());
        }

        log.info("[OpenSearch] mget 성공: 요청={}, 응답={}", docs.size(), results.size());
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

    // ============================================================
    // 응답 파싱용 record (내부용)
    // ============================================================

    /**
     * OpenSearch mget 응답의 최상위 구조
     */
    private record MgetResponse(
            List<MgetDoc> docs
    ) {}

    /**
     * mget 응답의 각 문서
     * _id, found, _source 는 OpenSearch가 반환하는 필드명
     */
    private record MgetDoc(
            @JsonProperty("_id") String id,
            Boolean found,

            @JsonProperty("_source") PatentDocument source
    ) {}
}
