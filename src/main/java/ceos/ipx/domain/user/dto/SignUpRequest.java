package ceos.ipx.domain.user.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Sign-up request")
public record SignUpRequest(
        @Schema(description = "Verified email address", example = "abcd@gmail.com")
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be a valid format.")
        String email,

        @Schema(
                description = "Email verification token returned after email verification",
                example = "email-verification-token-example"
        )
        @NotBlank(message = "Verification token is required.")
        String verificationToken,

        @Schema(description = "User name", example = "Kim Planner")
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must be 100 characters or fewer.")
        String name,

        @Schema(description = "Password", example = "Password123!")
        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
        String password,

        @Schema(description = "Password confirmation", example = "Password123!")
        @NotBlank(message = "Password confirmation is required.")
        @Size(min = 8, max = 20, message = "Password confirmation must be between 8 and 20 characters.")
        String passwordConfirm,

        @Schema(description = "Company name", example = "IPX", nullable = true)
        @Size(max = 200, message = "Company must be 200 characters or fewer.")
        String company,

        @ArraySchema(
                schema = @Schema(implementation = TermsAgreementRequest.class),
                arraySchema = @Schema(description = "List of terms agreements")
        )
        @NotEmpty(message = "At least one terms agreement is required.")
        List<@Valid TermsAgreementRequest> termsAgreements
) {
}
