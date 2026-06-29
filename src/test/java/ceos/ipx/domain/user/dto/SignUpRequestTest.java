package ceos.ipx.domain.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SignUpRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void validPasswordsPassComplexityValidation() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "verification-token",
                "Test User",
                "Password123!",
                "Password123!",
                "IPX",
                List.of(new TermsAgreementRequest("SERVICE", true))
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void invalidPasswordAndConfirmationFailComplexityValidation() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "verification-token",
                "Test User",
                "Password123",
                "Password!!!",
                "IPX",
                List.of(new TermsAgreementRequest("SERVICE", true))
        );

        Set<String> invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(invalidFields).containsExactlyInAnyOrder("password", "passwordConfirm");
    }
}
