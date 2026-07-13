package ceos.ipx.global.python.dto.response.inventivestep;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python /analyze/inventive-step/generate/combination-motivation
 *
 * background_limit: 종래기술의 근본적 한계 (100~200자)
 * teaching_away:    D1과 D2 결합 동기 부재 논증 (100~200자)
 */
public record PythonCombinationMotivationResult(

        @JsonProperty("background_limit")
        String backgroundLimit,

        @JsonProperty("teaching_away")
        String teachingAway
) {}