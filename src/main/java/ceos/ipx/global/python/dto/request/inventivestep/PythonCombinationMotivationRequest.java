package ceos.ipx.global.python.dto.request.inventivestep;

import ceos.ipx.global.python.dto.common.PythonPriorArtInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Python /analyze/inventive-step/generate/combination-motivation
 *
 * 복수인용발명결합 (Teaching Away) 논리 생성
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonCombinationMotivationRequest(

        @JsonProperty("invention_title")
        String inventionTitle,

        @JsonProperty("invention_description")
        String inventionDescription,

        @JsonProperty("primary_art")
        PythonPriorArtInfo primaryArt,

        @JsonProperty("secondary_art")
        PythonPriorArtInfo secondaryArt,

        @JsonProperty("prior_art_reference")
        String priorArtReference,

        @JsonProperty("differentiation_notes")
        String differentiationNotes
) {}