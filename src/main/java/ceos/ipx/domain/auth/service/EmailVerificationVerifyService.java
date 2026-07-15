package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.EmailVerificationPurpose;
import ceos.ipx.domain.auth.dto.EmailVerificationVerifyRequest;
import ceos.ipx.domain.auth.dto.EmailVerificationVerifyResponse;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.entity.UserProvider;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationVerifyService {

    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    public EmailVerificationVerifyResponse verifyEmailVerificationCode(EmailVerificationVerifyRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        EmailVerificationPurpose purpose = request.getPurpose();

        if (purpose == EmailVerificationPurpose.PASSWORD_RESET) {
            validatePasswordResetTargetUser(email);
        }

        String verificationToken = emailVerificationService.verifyVerificationCode(purpose, email, code);

        return EmailVerificationVerifyResponse.builder()
                .email(email)
                .purpose(purpose)
                .verified(true)
                .verificationToken(verificationToken)
                .expiresIn(emailVerificationService.getVerifiedExpiresInSeconds())
                .build();
    }

    private void validatePasswordResetTargetUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        if (user.getProvider() != UserProvider.LOCAL) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED);
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED);
        }
    }
}