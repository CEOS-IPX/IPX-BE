package ceos.ipx.global.python.dto.response.inventivestep;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python /analyze/inventive-step/generate/simple-design
 *
 * changed_component: 반박 대상 구성요소 라벨 (A/B/C/...)
 * non_obviousness:   단순 설계 변경이 아닌 비자명한 개선이라는 논리 (150~250자)
 */
public record PythonSimpleDesignResult(

        @JsonProperty("changed_component")
        String changedComponent,

        @JsonProperty("non_obviousness")
        String nonObviousness
) {}