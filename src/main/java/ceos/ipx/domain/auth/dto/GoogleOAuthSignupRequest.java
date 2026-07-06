package ceos.ipx.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Google OAuth 추가 회원가입 요청")
public record GoogleOAuthSignupRequest(

        @NotBlank
        @Schema(
                description = "Google OAuth token 교환 API에서 발급된 추가 회원가입 토큰",
                example = "2d9c400f-7777-4399-a26a-602f18ca7509"
        )
        String oauthSignupToken,

        @Size(max = 200)
        @Schema(description = "회사명", example = "IPX", nullable = true)
        String company
) {
}