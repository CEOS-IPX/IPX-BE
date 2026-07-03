package ceos.ipx.domain.auth.dto;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EmailVerificationPurpose {

    SIGNUP("signup"),
    PASSWORD_RESET("password_reset");

    private final String value;

    @JsonCreator
    public static EmailVerificationPurpose from(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return Arrays.stream(values())
                .filter(purpose ->
                        purpose.value.equalsIgnoreCase(value)
                                || purpose.name().equalsIgnoreCase(value)
                )
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_EMAIL_VERIFICATION_PURPOSE));
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}