package ceos.ipx.global.python.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Python /components/extract 요청 DTO
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonComponentExtractRequest(
        String title,

        String description,

        @JsonProperty("technical_field")
        String technicalField
) {}