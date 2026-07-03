package ceos.ipx.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Terms agreement item")
public record TermsAgreementRequest(
        @Schema(description = "약관 타입", example = "SERVICE")
        @NotBlank(message = "약관 타입은 필수입니다.")
        String type,

        @Schema(description = "약관 동의 여부", example = "true")
        @NotNull(message = "약관 동의 여부는 필수입니다.")
        Boolean agreed
) {
}