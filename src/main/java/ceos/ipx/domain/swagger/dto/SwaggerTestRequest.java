package ceos.ipx.domain.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "SwaggerTestRequest", description = "Swagger UI POST test request payload.")
public record SwaggerTestRequest(
    @Schema(description = "Email used for Swagger UI mock login testing.", example = "tester@example.com")
    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    String email,

    @Schema(description = "Password used for Swagger UI mock login testing.", example = "Passw0rd!")
    @NotBlank(message = "password is required")
    String password
) {
}
