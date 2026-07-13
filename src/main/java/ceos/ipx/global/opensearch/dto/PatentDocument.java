package ceos.ipx.global.opensearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenSearch에서 조회한 특허 문서
 *
 * mget으로 조회할 때 각 hit의 _source에 매핑되는 구조
 *
 * 주의: claims_independent는 OpenSearch에 배열로 저장
 *      Python 전달 시 개행으로 join해서 String으로 변환 필요
 */
public record PatentDocument(

        @JsonProperty("application_number")
        String applicationNumber,

        String title,

        @JsonProperty("abstract_clean")
        String abstractClean,

        /**
         * 독립 청구항 리스트
         * 각 항목은 "청구항 N: 본문" 형식
         * OpenSearch에는 List로 저장, Python에는 개행 join된 String으로 전달
         */
        @JsonProperty("claims_independent")
        List<String> claimsIndependent
) {

        /**
         * 청구항 리스트를 개행으로 join한 문자열 반환
         * Python API 전달용
         */
        public String claimsIndependentAsString() {
                if (claimsIndependent == null || claimsIndependent.isEmpty()) {
                        return "";
                }
                return String.join("\n", claimsIndependent);
        }
}