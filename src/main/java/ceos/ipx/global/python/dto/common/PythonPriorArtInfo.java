package ceos.ipx.global.python.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Python으로 전달하는 선행기술 정보
 *
 * Python 측 PriorArtInfo와 필드명 일치
 *
 * OpenSearch에서 조회 시 abstract_clean 필드에서 값을 가져옴 (Service 조립 시)
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonPriorArtInfo(

        @JsonProperty("application_number")
        String applicationNumber,

        String title,

        @JsonProperty("abstract")
        String abstractText,

        @JsonProperty("claims_independent")
        String claimsIndependent,

        @JsonProperty("tech_purpose")
        String techPurpose
) {}