package ceos.ipx.global.opensearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenSearch에서 조회한 특허 문서
 *
 * mget으로 조회할 때 각 hit의 _source에 매핑되는 구조
 * 진보성/신규성 분석에 필요한 필드만 포함
 */
public record PatentDocument(

        @JsonProperty("application_number")
        String applicationNumber,

        String title,

        /**
         * 초록 (전처리 완료 텍스트)
         */
        @JsonProperty("abstract_clean")
        String abstractClean,

        /**
         * 독립 청구항
         * OpenSearch에서는 text 타입 단일 필드
         * "청구항 N: 본문" 형식이 개행으로 구분되어 저장됨
         */
        @JsonProperty("claims_independent")
        String claimsIndependent
) {}