package ceos.ipx.domain.auth.dto;

import ceos.ipx.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 사용자 정보 응답")
public record LoginUserResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "test@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "회사명", example = "IPX")
        String company,

        @Schema(description = "가입 제공자", example = "LOCAL")
        String provider,

        @Schema(description = "프로필 완료 여부", example = "true")
        boolean profileCompleted
) {

    public static LoginUserResponse from(User user) {
        return new LoginUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCompany(),
                user.getProvider().name(),
                isProfileCompleted(user)
        );
    }

    private static boolean isProfileCompleted(User user) {
        return user.getName() != null && !user.getName().isBlank();
    }
}