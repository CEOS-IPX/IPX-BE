package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.EmailVerificationPurpose;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender javaMailSender;

    public void sendVerificationCode(String toEmail, String code, EmailVerificationPurpose purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(createSubject(purpose));
            message.setText(createVerificationCodeMessage(code, purpose));

            javaMailSender.send(message);

            log.info("이메일 인증 코드 발송 완료. email={}, purpose={}", toEmail, purpose);
        } catch (MailException e) {
            log.error("이메일 인증 코드 발송 실패. email={}, purpose={}", toEmail, purpose, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String createSubject(EmailVerificationPurpose purpose) {
        if (purpose == EmailVerificationPurpose.PASSWORD_RESET) {
            return "[IPX] 비밀번호 재설정 인증 코드 안내";
        }

        return "[IPX] 이메일 인증 코드 안내";
    }

    private String createVerificationCodeMessage(String code, EmailVerificationPurpose purpose) {
        if (purpose == EmailVerificationPurpose.PASSWORD_RESET) {
            return """
                    안녕하세요. IPX입니다.

                    비밀번호 재설정을 위한 인증 코드는 아래와 같습니다.

                    인증 코드: %s

                    인증 코드는 3분 동안 유효합니다.
                    본인이 비밀번호 재설정을 요청하지 않았다면 이 메일을 무시해주세요.
                    """.formatted(code);
        }

        return """
                안녕하세요. IPX입니다.

                이메일 인증을 위한 인증 코드는 아래와 같습니다.

                인증 코드: %s

                인증 코드는 3분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code);
    }
}