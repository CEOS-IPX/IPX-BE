package ceos.ipx.domain.cases.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 구성요소 자동 추출 요청
 */
public record ComponentExtractRequest(
        @NotBlank
        @Size(max = 500)
        String title,

        @NotBlank
        String description,

        String technicalField
) {}
