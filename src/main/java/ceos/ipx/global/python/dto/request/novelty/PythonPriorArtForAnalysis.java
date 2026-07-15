package ceos.ipx.global.python.dto.request.novelty;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 신규성 분석용 선행기술 정보
 *
 * Python 측 PriorArtForAnalysis와 필드명 일치:
 *   - application_number, title, claims_independent
 */
@Builder
public record PythonPriorArtForAnalysis(

        @JsonProperty("application_number")
        String applicationNumber,

        String title,

        /**
         * 독립 청구항
         * "청구항 N: 본문" 형식이 개행으로 join된 문자열
         */
        @JsonProperty("claims_independent")
        String claimsIndependent
) {}