package ceos.ipx.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 회원가입 필요 응답")
public record OAuthSignupRequiredResponse(

        @Schema(description = "OAuth 회원가입 토큰")
        String oauthSignupToken,

        @Schema(description = "Google 이메일", example = "test@gmail.com")
        String email,

        @Schema(description = "Google 이름", example = "홍길동")
        String name,

        @Schema(description = "가입 제공자", example = "GOOGLE")
        String provider
) {
}