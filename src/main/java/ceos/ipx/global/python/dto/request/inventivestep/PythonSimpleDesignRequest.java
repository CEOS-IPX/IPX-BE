package ceos.ipx.global.python.dto.request.inventivestep;

import ceos.ipx.global.python.dto.shared.PythonInventionComponent;
import ceos.ipx.global.python.dto.shared.PythonPriorArtInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Python /analyze/inventive-step/generate/simple-design
 *
 * 단순설계변경 반박 (비자명성) 논리 생성
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonSimpleDesignRequest(

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