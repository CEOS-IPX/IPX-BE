package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.EmailVerificationPurpose;
import ceos.ipx.domain.auth.dto.EmailVerificationSendRequest;
import ceos.ipx.domain.auth.dto.EmailVerificationSendResponse;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.entity.UserProvider;
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

        if (purpose == EmailVerificationPurpose.SIGNUP) {
            validateSignupEmail(email);
        }

        if (purpose == EmailVerificationPurpose.PASSWORD_RESET) {
            validatePasswordResetEmail(email);
        }

        String verificationCode = emailVerificationService.createAndSaveVerificationCode(purpose, email);

        emailSenderService.sendVerificationCode(email, verificationCode, purpose);

        return EmailVerificationSendResponse.builder()
                .email(email)
                .purpose(purpose)
                .expiresIn(emailVerificationService.getCodeExpiresInSeconds())
                .resendAvailableIn(emailVerificationService.getResendAvailableInSeconds())
                .build();
    }

    private void validateSignupEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private void validatePasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getProvider() != UserProvider.LOCAL) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED);
        }
    }
}