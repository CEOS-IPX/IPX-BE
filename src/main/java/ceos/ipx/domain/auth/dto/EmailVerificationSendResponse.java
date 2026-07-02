package ceos.ipx.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerificationSendResponse {

    private String email;
    private EmailVerificationPurpose purpose;
    private int expiresIn;
    private int resendAvailableIn;
}