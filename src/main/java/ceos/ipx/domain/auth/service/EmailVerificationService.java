package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.EmailVerificationPurpose;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRES_IN_SECONDS = 180;
    private static final int RESEND_AVAILABLE_IN_SECONDS = 60;

    private static final String CODE_KEY_PREFIX = "emailVerification:code:";
    private static final String COOLDOWN_KEY_PREFIX = "emailVerification:cooldown:";

    private static final int VERIFIED_EXPIRES_IN_SECONDS = 600;

    private static final String VERIFIED_KEY_PREFIX = "emailVerification:verified:";
    private static final String TOKEN_KEY_PREFIX = "emailVerification:token:";

    private final StringRedisTemplate stringRedisTemplate;

    public String createAndSaveVerificationCode(EmailVerificationPurpose purpose, String email) {
        String cooldownKey = createCooldownKey(purpose, email);

        Boolean hasCooldown = stringRedisTemplate.hasKey(cooldownKey);
        if (Boolean.TRUE.equals(hasCooldown)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_RESEND_TOO_EARLY);
        }

        String code = generateCode();

        stringRedisTemplate.opsForValue().set(
                createCodeKey(purpose, email),
                code,
                Duration.ofSeconds(CODE_EXPIRES_IN_SECONDS)
        );

        stringRedisTemplate.opsForValue().set(
                cooldownKey,
                "1",
                Duration.ofSeconds(RESEND_AVAILABLE_IN_SECONDS)
        );

        return code;
    }

    public String verifyVerificationCode(EmailVerificationPurpose purpose, String email, String code) {
        String codeKey = createCodeKey(purpose, email);
        String savedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (savedCode == null) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!savedCode.equals(code)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        stringRedisTemplate.delete(codeKey);

        String verificationToken = createVerificationToken();

        stringRedisTemplate.opsForValue().set(
                createVerifiedKey(purpose, email),
                verificationToken,
                Duration.ofSeconds(VERIFIED_EXPIRES_IN_SECONDS)
        );

        stringRedisTemplate.opsForValue().set(
                createTokenKey(purpose, verificationToken),
                email,
                Duration.ofSeconds(VERIFIED_EXPIRES_IN_SECONDS)
        );

        return verificationToken;
    }

    public int getCodeExpiresInSeconds() {
        return CODE_EXPIRES_IN_SECONDS;
    }

    public int getResendAvailableInSeconds() {
        return RESEND_AVAILABLE_IN_SECONDS;
    }

    public int getVerifiedExpiresInSeconds() {
        return VERIFIED_EXPIRES_IN_SECONDS;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    public String getEmailByPasswordResetToken(String verificationToken) {
        String tokenKey = createTokenKey(EmailVerificationPurpose.PASSWORD_RESET, verificationToken);
        String email = stringRedisTemplate.opsForValue().get(tokenKey);

        if (email == null) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        return email;
    }

    public void deletePasswordResetVerification(String verificationToken, String email) {
        stringRedisTemplate.delete(createTokenKey(EmailVerificationPurpose.PASSWORD_RESET, verificationToken));
        stringRedisTemplate.delete(createVerifiedKey(EmailVerificationPurpose.PASSWORD_RESET, email));
        stringRedisTemplate.delete(createCodeKey(EmailVerificationPurpose.PASSWORD_RESET, email));
    }

    private String createCodeKey(EmailVerificationPurpose purpose, String email) {
        return CODE_KEY_PREFIX + purpose.getValue() + ":" + email;
    }

    private String createCooldownKey(EmailVerificationPurpose purpose, String email) {
        return COOLDOWN_KEY_PREFIX + purpose.getValue() + ":" + email;
    }

    private String createVerifiedKey(EmailVerificationPurpose purpose, String email) {
        return VERIFIED_KEY_PREFIX + purpose.getValue() + ":" + email;
    }

    private String createTokenKey(EmailVerificationPurpose purpose, String verificationToken) {
        return TOKEN_KEY_PREFIX + purpose.getValue() + ":" + verificationToken;
    }

    private String createVerificationToken() {
        return UUID.randomUUID().toString();
    }
}