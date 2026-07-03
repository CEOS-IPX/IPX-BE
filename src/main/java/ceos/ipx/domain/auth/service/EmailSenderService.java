package ceos.ipx.domain.auth.service;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender javaMailSender;

    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[IPX] 이메일 인증 코드 안내");
            message.setText(createVerificationCodeMessage(code));

            javaMailSender.send(message);

            log.info("이메일 인증 코드 발송 완료. email={}", toEmail);
        } catch (MailException e) {
            log.error("이메일 인증 코드 발송 실패. email={}", toEmail, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String createVerificationCodeMessage(String code) {
        return """
                안녕하세요. IPX입니다.

                이메일 인증을 위한 인증 코드는 아래와 같습니다.

                인증 코드: %s

                인증 코드는 3분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code);
    }
}