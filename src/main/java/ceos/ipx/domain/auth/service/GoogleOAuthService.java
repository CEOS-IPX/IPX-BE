package ceos.ipx.domain.auth.service;

import ceos.ipx.global.config.FrontendOAuthProperties;
import ceos.ipx.global.config.GoogleOAuthProperties;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private static final String RESPONSE_TYPE_CODE = "code";
    private static final String ACCESS_TYPE_OFFLINE = "offline";
    private static final String PROMPT_CONSENT = "consent";

    private final GoogleOAuthProperties googleOAuthProperties;
    private final FrontendOAuthProperties frontendOAuthProperties;
    private final GoogleOAuthStateService googleOAuthStateService;

    public String generateGoogleAuthUrl(String redirectUri) {
        String frontendRedirectUri = resolveFrontendRedirectUri(redirectUri);
        String state = UUID.randomUUID().toString();

        googleOAuthStateService.saveState(state, frontendRedirectUri);

        try {
            return UriComponentsBuilder
                    .fromUriString(googleOAuthProperties.authUri())
                    .queryParam("client_id", googleOAuthProperties.clientId())
                    .queryParam("redirect_uri", googleOAuthProperties.redirectUri())
                    .queryParam("response_type", RESPONSE_TYPE_CODE)
                    .queryParam("scope", String.join(" ", googleOAuthProperties.scope()))
                    .queryParam("state", state)
                    .queryParam("access_type", ACCESS_TYPE_OFFLINE)
                    .queryParam("prompt", PROMPT_CONSENT)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.OAUTH_URL_GENERATION_FAILED);
        }
    }

    public String buildFrontendCallbackUrl(String code, String state) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String frontendRedirectUri = googleOAuthStateService.consumeState(state);

        return UriComponentsBuilder
                .fromUriString(frontendRedirectUri)
                .queryParam("code", code)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    private String resolveFrontendRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            return frontendOAuthProperties.googleCallbackUri();
        }

        if (!isAllowedFrontendRedirectUri(redirectUri)) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_REDIRECT_URI);
        }

        return redirectUri;
    }

    private boolean isAllowedFrontendRedirectUri(String redirectUri) {
        return frontendOAuthProperties.allowedRedirectUris() != null
                && frontendOAuthProperties.allowedRedirectUris().contains(redirectUri);
    }
}