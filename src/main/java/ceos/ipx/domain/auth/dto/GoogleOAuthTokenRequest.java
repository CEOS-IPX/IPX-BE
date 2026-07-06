package ceos.ipx.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Google OAuth code 토큰 교환 요청")
public record GoogleOAuthTokenRequest(

        @NotBlank(message = "Google Authorization Code는 필수입니다.")
        @Schema(description = "Google Authorization Code", example = "4/0AbCdEf...")
        String code
) {
}