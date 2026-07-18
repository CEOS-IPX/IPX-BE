package ceos.ipx.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청한 리소스를 찾을 수 없습니다."),

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
    SOCIAL_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED(HttpStatus.FORBIDDEN, "AU012", "소셜 로그인 계정은 비밀번호를 재설정할 수 없습니다."),
    INVALID_EMAIL_VERIFICATION_PURPOSE(HttpStatus.BAD_REQUEST, "AU013", "잘못된 이메일 인증 목적입니다."),
    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.GONE, "AU014", "비밀번호 재설정 토큰이 만료되었거나 존재하지 않습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AU015", "비밀번호 형식이 올바르지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "AU016", "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
    INVALID_OAUTH_REDIRECT_URI(HttpStatus.BAD_REQUEST, "AU017", "허용되지 않은 OAuth redirectUri입니다."),
    OAUTH_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AU018", "OAuth URL 생성에 실패했습니다."),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "AU019", "유효하지 않은 OAuth state입니다."),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "AU020", "Google 토큰 교환에 실패했습니다."),
    OAUTH_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "AU021", "Google 사용자 정보 조회에 실패했습니다."),
    SOCIAL_LOGIN_NOT_ALLOWED(HttpStatus.CONFLICT, "AU022", "일반 로그인으로 가입된 이메일입니다."),
    OAUTH_SIGNUP_TOKEN_EXPIRED(HttpStatus.GONE, "AU023", "OAuth 회원가입 토큰이 만료되었거나 존재하지 않습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "AU024", "필수 약관에 동의해야 합니다."),
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.GONE, "AU025", "이메일 인증 토큰이 만료되었거나 존재하지 않습니다."),
    EMAIL_VERIFICATION_EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "AU026", "이메일 인증 정보가 일치하지 않습니다."),

    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "SC001", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "SC002", "해당 요청에 권한이 없습니다."),

    // ===== Python 서버 통신 =====
    PYTHON_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "PY001", "AI 서버와 통신 중 오류가 발생했습니다."),
    PYTHON_SERVER_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "PY002", "AI 서버 응답 시간이 초과되었습니다."),
    INVALID_PYTHON_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "PY003", "AI 서버 응답 형식이 올바르지 않습니다."),

    // ===== 사건 관련 =====
    CASE_NOT_FOUND(HttpStatus.NOT_FOUND, "CA001", "사건을 찾을 수 없습니다."),
    CASE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CA002", "해당 사건에 접근할 권한이 없습니다."),

    // ===== 검색 관련 =====
    SEARCH_NOT_STARTED(HttpStatus.BAD_REQUEST, "S001", "검색이 시작되지 않았습니다."),
    SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, "S002", "검색 정보를 찾을 수 없습니다."),
    COMPONENTS_REQUIRED(HttpStatus.BAD_REQUEST, "S003", "구성요소가 최소 1개 이상 필요합니다."),

    // ===== 선행기술 관련 =====
    PRIOR_ART_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "해당 선행기술을 찾을 수 없습니다."),
    ALL_PATENTS_ALREADY_EXIST(HttpStatus.CONFLICT, "P002", "모든 특허가 이미 추가되어 있습니다."),
    PRIOR_ART_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "선행기술 문서를 찾을 수 없습니다."),

    // ==== OpenSearch 관련 ====
    OPENSEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "O001", "특허 검색 서버와 통신 중 오류가 발생했습니다."),

    // ==== 신규성 분석 관련 ====
    NOVELTY_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "신규성 분석 결과가 존재하지 않습니다."),

    // ==== 진보성 분석 관련 ====
    INVENTIVE_STEP_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "I003", "진보성 분석 결과가 존재하지 않습니다."),
    INVENTIVE_ARGUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "I004", "진보성 분석 항목이 존재하지 않습니다."),
    INVENTIVE_STEP_NO_CANDIDATES(HttpStatus.BAD_REQUEST, "I001", "부인용 D2로 사용할 후보가 없습니다."),
    INVENTIVE_STEP_INSUFFICIENT_PRIOR_ARTS(HttpStatus.BAD_REQUEST, "I002", "진보성 분석에는 최소 2개의 선행기술이 필요합니다."),

    // ==== 분석 리포트 관련 ====
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "RP001", "이미 분석 리포트가 존재합니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "RP002", "분석 리포트를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}