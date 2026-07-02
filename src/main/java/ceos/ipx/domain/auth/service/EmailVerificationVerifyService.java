package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.EmailVerificationPurpose;
import ceos.ipx.domain.auth.dto.EmailVerificationVerifyRequest;
import ceos.ipx.domain.auth.dto.EmailVerificationVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationVerifyService {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationVerifyResponse verifyEmailVerificationCode(EmailVerificationVerifyRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        EmailVerificationPurpose purpose = request.getPurpose();

        String verificationToken = emailVerificationService.verifyVerificationCode(purpose, email, code);

        return EmailVerificationVerifyResponse.builder()
                .email(email)
                .purpose(purpose)
                .verified(true)
                .verificationToken(verificationToken)
                .build();
    }
}