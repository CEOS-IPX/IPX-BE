package ceos.ipx.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "AccessToken 재발급 응답")
public class ReissueResponse {

    @Schema(description = "새로 발급된 AccessToken", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "AccessToken 만료 시간, 초 단위", example = "3600")
    private Long expiresIn;
}