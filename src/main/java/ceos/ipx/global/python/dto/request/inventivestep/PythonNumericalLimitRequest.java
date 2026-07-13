package ceos.ipx.global.python.dto.request.inventivestep;

import ceos.ipx.global.python.dto.shared.PythonPriorArtInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Python /analyze/inventive-step/generate/numerical-limit
 *
 * 수치한정 논리 생성
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonNumericalLimitRequest(

        @JsonProperty("invention_title")
        String inventionTitle,

        @JsonProperty("invention_description")
        String inventionDescription,

        @JsonProperty("primary_art")
        PythonPriorArtInfo primaryArt,

        @JsonProperty("measurement_conditions")
        String measurementConditions,

        @JsonProperty("measurement_results")
        String measurementResults
) {}