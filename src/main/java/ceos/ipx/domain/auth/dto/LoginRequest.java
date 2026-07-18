package ceos.ipx.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "일반 로그인 요청")
public record LoginRequest(

        @Schema(description = "이메일", example = "abcd@gmail.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "비밀번호", example = "Password123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @Schema(description = "로그인 유지 여부", example = "false")
        boolean rememberMe
) {
}