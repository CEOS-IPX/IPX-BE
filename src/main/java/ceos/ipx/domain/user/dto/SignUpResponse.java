package ceos.ipx.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sign-up response")
public record SignUpResponse(
        @Schema(description = "Created user ID", example = "1")
        Long userId,

        @Schema(description = "Signed-up user email", example = "abcd@gmail.com")
        String email,

        @Schema(description = "User name", example = "Kim Planner")
        String name,

        @Schema(description = "Company name", example = "IPX", nullable = true)
        String company,

        @Schema(description = "Authentication provider", example = "LOCAL")
        String provider,

        @Schema(description = "Whether the user profile is completed", example = "true")
        boolean profileCompleted
) {
}
