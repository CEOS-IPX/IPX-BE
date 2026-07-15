package ceos.ipx.global.python.dto.request.inventivestep;

import ceos.ipx.global.python.dto.common.PythonInventionComponent;
import ceos.ipx.global.python.dto.common.PythonPriorArtInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Python /analyze/inventive-step/select-categories
 *
 * 4개 카테고리 중 이슈가 될 만한 것들을 자동 선정
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonSelectCategoriesRequest(

        @JsonProperty("invention_title")
        String inventionTitle,

        @JsonProperty("invention_description")
        String inventionDescription,

        List<PythonInventionComponent> components,

        @JsonProperty("primary_art")
        PythonPriorArtInfo primaryArt,

        @JsonProperty("secondary_art")
        PythonPriorArtInfo secondaryArt,

        @JsonProperty("prior_art_reference")
        String priorArtReference,

        @JsonProperty("differentiation_notes")
        String differentiationNotes,

        @JsonProperty("measurement_conditions")
        String measurementConditions,

        @JsonProperty("measurement_results")
        String measurementResults
) {}