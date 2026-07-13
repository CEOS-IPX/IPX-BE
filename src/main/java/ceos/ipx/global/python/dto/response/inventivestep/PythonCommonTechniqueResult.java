package ceos.ipx.global.python.dto.response.inventivestep;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python /analyze/inventive-step/generate/common-technique
 *
 * target_component: 반박 대상 구성요소 라벨 (A/B/C/...)
 * rebuttal:         주지관용기술이 아니라는 반박 논리 (150~250자)
 */
public record PythonCommonTechniqueResult(

        @JsonProperty("target_component")
        String targetComponent,

        String rebuttal
) {}