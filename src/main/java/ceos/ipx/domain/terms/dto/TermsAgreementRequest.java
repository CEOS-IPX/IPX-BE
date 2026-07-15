package ceos.ipx.domain.terms.dto;

import ceos.ipx.domain.terms.entity.TermsAgreementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "약관 동의 요청")
public record TermsAgreementRequest(

        @Schema(description = "약관 타입", example = "SERVICE_TERMS")
        @NotNull(message = "약관 타입은 필수입니다.")
        TermsAgreementType type,

        @Schema(description = "약관 동의 여부", example = "true")
        @NotNull(message = "약관 동의 여부는 필수입니다.")
        Boolean agreed
) {
}