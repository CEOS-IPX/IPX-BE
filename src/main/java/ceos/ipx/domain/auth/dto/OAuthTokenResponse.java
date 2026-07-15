package ceos.ipx.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth token 교환 결과 응답")
public record OAuthTokenResponse(

        @Schema(description = "OAuth 로그인 처리 상태", example = "LOGIN_SUCCESS")
        OAuthLoginStatus status,

        @Schema(description = "JWT AccessToken")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "AccessToken 만료 시간(초)", example = "3600")
        Long expiresIn,

        @Schema(description = "로그인한 사용자 정보")
        LoginUserResponse user,

        @Schema(description = "OAuth 회원가입 토큰")
        String oauthSignupToken,

        @Schema(description = "Google 이메일", example = "test@gmail.com")
        String email,

        @Schema(description = "Google 이름", example = "홍길동")
        String name,

        @Schema(description = "가입 제공자", example = "GOOGLE")
        String provider
) {
    public static OAuthTokenResponse loginSuccess(LoginResponse loginResponse) {
        return new OAuthTokenResponse(
                OAuthLoginStatus.LOGIN_SUCCESS,
                loginResponse.accessToken(),
                loginResponse.tokenType(),
                loginResponse.expiresIn(),
                loginResponse.user(),
                null,
                null,
                null,
                null
        );
    }

    public static OAuthTokenResponse needSignup(OAuthSignupRequiredResponse signupResponse) {
        return new OAuthTokenResponse(
                OAuthLoginStatus.NEED_SIGNUP,
                null,
                null,
                null,
                null,
                signupResponse.oauthSignupToken(),
                signupResponse.email(),
                signupResponse.name(),
                signupResponse.provider()
        );
    }
}