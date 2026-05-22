package ceos.ipx.domain.swagger.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SwaggerTestResponse", description = "Swagger UI mock response payload.")
public record SwaggerTestResponse(
    @Schema(description = "Health status for GET test API.", example = "UP")
    String status,

    @Schema(description = "Mock response message.", example = "Swagger test API is available.")
    String message,

    @Schema(description = "Mock access token for POST test API.", example = "mock-access-token")
    String accessToken,

    @Schema(description = "Mock token type for POST test API.", example = "Bearer")
    String tokenType,

    @Schema(description = "Echoed email from the login request.", example = "tester@example.com")
    String email
) {
    public static SwaggerTestResponse health() {
        return new SwaggerTestResponse(
            "UP",
            "Swagger test API is available.",
            null,
            null,
            null
        );
    }

    public static SwaggerTestResponse login(String email) {
        return new SwaggerTestResponse(
            null,
            "Mock login succeeded.",
            "mock-access-token",
            "Bearer",
            email
        );
    }
}
