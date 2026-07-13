package ceos.ipx.global.python.dto.request.inventivestep;

import ceos.ipx.global.python.dto.common.PythonInventionComponent;
import ceos.ipx.global.python.dto.common.PythonPriorArtInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Python /analyze/inventive-step/select-secondary
 *
 * D1이 주어졌을 때 D2를 후보 중에서 선정.
 */
@Builder
public record PythonSelectSecondaryRequest(

        @JsonProperty("invention_title")
        String inventionTitle,

        @JsonProperty("invention_description")
        String inventionDescription,

        List<PythonInventionComponent> components,

        @JsonProperty("primary_art")
        PythonPriorArtInfo primaryArt,

        /**
         * D2 후보 (D1 제외한 나머지 prior_arts 중 상위 N건)
         */
        List<PythonPriorArtInfo> candidates
) {}
