package ceos.ipx.global.python.dto.response.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Python 서버의 검색 응답 DTO.
 *
 * Python /search 엔드포인트 응답 스펙:
 *   - case_id
 *   - is_valid: 요청이 유효했는지
 *   - reason_invalid: is_valid=false일 때 사유
 *   - intent: 검색 의도 (IntentResult)
 *   - results: 특허 결과 리스트
 *   - debug: 디버그 정보 (Optional)
 */
public record PythonSearchResultResponse(
        @JsonProperty("case_id")
        String caseId,

        @JsonProperty("is_valid")
        Boolean isValid,

        @JsonProperty("reason_invalid")
        String reasonInvalid,

        Intent intent,

        List<PatentResult> results,

        Debug debug
) {

    /**
     * Python이 사용자 발명으로부터 추출한 검색 의도.
     * Case에 저장할 keywords는 여기서 가져옴.
     *
     * Python IntentResult 스펙:
     *   - is_valid, reason_invalid: 최상위와 중복 (참고용)
     *   - keywords: LLM이 추출한 핵심 키워드 → Case.keywords 저장
     *   - ipc_codes: LLM이 추정한 IPC 코드
     */
    public record Intent(
            @JsonProperty("is_valid")
            Boolean isValid,

            @JsonProperty("reason_invalid")
            String reasonInvalid,

            List<String> keywords,

            @JsonProperty("ipc_codes")
            List<String> ipcCodes
    ) {}

    /**
     * 개별 특허 결과
     * Spring 측 prior_arts 테이블에 저장할 정보
     */
    public record PatentResult(
            @JsonProperty("application_number")
            String applicationNumber,

            String title,

            @JsonProperty("applicant_name")
            String applicantName,

            @JsonProperty("application_date")
            String applicationDate,

            @JsonProperty("registration_date")
            String registrationDate,

            @JsonProperty("legal_status")
            String legalStatus,

            @JsonProperty("ipc_codes")
            List<String> ipcCodes,

            @JsonProperty("relevance_score")
            Integer relevanceScore,
            String summary,
            String purpose,
            List<String> features,
            List<String> keywords,
            String reason,

            @JsonProperty("rrf_score")
            Double rrfScore,

            /**
             * 데이터 소스: ["opensearch", "pgvector", "manual"] 중 하나 이상
             * Spring에서 이 리스트를 보고 PriorArtSource enum(SEARCH/MANUAL) 결정
             */
            List<String> sources
    ) {}

    /**
     * 디버그 정보 (Python SearchDebugInfo와 필드 일치)
     */
    public record Debug(
            @JsonProperty("expanded_keywords")
            List<String> expandedKeywords,

            @JsonProperty("trusted_ipc")
            List<String> trustedIpc,

            @JsonProperty("estimated_ipc")
            List<String> estimatedIpc,

            @JsonProperty("hypothetical_abstract")
            String hypotheticalAbstract,

            @JsonProperty("embedding_dim")
            Integer embeddingDim,

            @JsonProperty("opensearch_count")
            Integer opensearchCount,

            @JsonProperty("pgvector_count")
            Integer pgvectorCount,

            @JsonProperty("merged_unique")
            Integer mergedUnique
    ) {}
}
