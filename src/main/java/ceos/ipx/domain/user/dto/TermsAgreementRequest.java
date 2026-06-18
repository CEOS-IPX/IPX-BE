package ceos.ipx.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Terms agreement item")
public record TermsAgreementRequest(
        @Schema(description = "Terms type", example = "SERVICE")
        @NotBlank(message = "Terms type is required.")
        String type,

        @Schema(description = "Whether the user agreed to the terms", example = "true")
        @NotNull(message = "Terms agreement status is required.")
        Boolean agreed
) {
}
