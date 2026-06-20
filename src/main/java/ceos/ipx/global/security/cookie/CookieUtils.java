package ceos.ipx.global.security.cookie;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            long maxAgeSeconds
    ) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth/reissue")
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth/reissue")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String getRefreshTokenCookieName() {
        return REFRESH_TOKEN_COOKIE_NAME;
    }
}