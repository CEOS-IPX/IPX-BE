package ceos.ipx.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerificationVerifyResponse {

    private String email;
    private EmailVerificationPurpose purpose;
    private boolean verified;
    private String verificationToken;
    private int expiresIn;
}