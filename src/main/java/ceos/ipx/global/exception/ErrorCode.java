package ceos.ipx.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AU001", "이미 사용 중인 이메일입니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "AU002", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AU003", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "AU004", "비활성화된 계정입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AU005", "RefreshToken이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AU006", "유효하지 않은 RefreshToken입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AU007", "사용자를 찾을 수 없습니다."),
    EMAIL_VERIFICATION_RESEND_TOO_EARLY(HttpStatus.TOO_MANY_REQUESTS, "AU008", "인증 코드는 잠시 후 다시 요청할 수 있습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AU009", "이메일 발송에 실패했습니다."),
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.GONE, "AU010", "인증 코드가 만료되었거나 존재하지 않습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AU011", "인증 코드가 일치하지 않습니다."),

    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "SC001", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "SC002", "해당 요청에 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}