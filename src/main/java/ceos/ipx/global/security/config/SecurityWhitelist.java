package ceos.ipx.global.security.config;

// 화이트리스트 상수 클래스
public final class SecurityWhitelist {

    private SecurityWhitelist() {
        // 인스턴스 생성 방지
    }

    // ===== 누구나 접근 가능한 경로 =====
    public static final String[] PERMIT_ALL_PATHS = {
            "/health",

            // 인증 관련 API
            "/api/auth/signup",                  // 회원가입
            "/api/auth/login",                   // 로그인
            "/api/auth/logout",                  // 로그아웃
            "/api/auth/refresh",                 // 토큰 재발급
            "/api/auth/reissue",                 // AccessToken 재발급
            "/api/auth/email/send-otp",          // 이메일 OTP 발송
            "/api/auth/email/verify-otp",        // 이메일 OTP 인증
            "/api/auth/password/reset",          // 비밀번호 재설정 요청
            "/api/auth/password/reset/verify",   // 비밀번호 재설정 확인
            "/api/auth/email/send",

            // OAuth 콜백 (Google 로그인)
            "/oauth2/**",
            "/login/oauth2/**",

            // 헬스체크 / 모니터링
            "/actuator/health",
            "/actuator/info",

            // API 문서 (Swagger 사용 시)
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
    };

    // ===== 인증 필요한 경로 =====
    public static final String[] AUTHENTICATED_PATHS = {

            // 사용자 정보 관리
            "/api/users/**",                     // 이름 수정, 계정 삭제 등

            // 프로젝트 관리
            "/api/projects/**",                  // 프로젝트 CRUD, 후보 저장/삭제

            // 검색 (로그인한 사용자만 사용 가능하도록)
            "/api/search/**",                    // 특허 검색

            // 추가 인증 필요 작업
            "/api/auth/me"                       // 내 정보 조회
    };
}
