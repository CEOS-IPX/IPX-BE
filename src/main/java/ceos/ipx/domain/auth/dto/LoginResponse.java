package ceos.ipx.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일반 로그인 응답")
public record LoginResponse(

        @Schema(description = "JWT AccessToken")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "AccessToken 만료 시간(초)", example = "3600")
        long expiresIn,

        @Schema(description = "로그인한 사용자 정보")
        LoginUserResponse user
) {
}