package ceos.ipx.global.python.dto.request.inventivestep;

import ceos.ipx.global.python.dto.common.PythonInventionComponent;
import ceos.ipx.global.python.dto.common.PythonPriorArtInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Python /analyze/inventive-step/generate/common-technique
 *
 * 주지관용기술 반박 논리 생성.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonCommonTechniqueRequest(

        @JsonProperty("invention_title")
        String inventionTitle,

        @JsonProperty("invention_description")
        String inventionDescription,

        List<PythonInventionComponent> components,

        @JsonProperty("primary_art")
        PythonPriorArtInfo primaryArt,

        @JsonProperty("prior_art_reference")
        String priorArtReference,

        @JsonProperty("differentiation_notes")
        String differentiationNotes
) {}