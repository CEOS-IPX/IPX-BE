package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.EmailVerificationPurpose;
import ceos.ipx.domain.auth.dto.EmailVerificationSendRequest;
import ceos.ipx.domain.auth.dto.EmailVerificationSendResponse;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationSendService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final EmailSenderService emailSenderService;

    public EmailVerificationSendResponse sendEmailVerificationCode(EmailVerificationSendRequest request) {
        String email = request.getEmail();
        EmailVerificationPurpose purpose = request.getPurpose();

        if (purpose == EmailVerificationPurpose.SIGNUP && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String verificationCode = emailVerificationService.createAndSaveVerificationCode(purpose, email);

        emailSenderService.sendVerificationCode(email, verificationCode);

        return EmailVerificationSendResponse.builder()
                .email(email)
                .purpose(purpose)
                .expiresIn(emailVerificationService.getCodeExpiresInSeconds())
                .resendAvailableIn(emailVerificationService.getResendAvailableInSeconds())
                .build();
    }
}