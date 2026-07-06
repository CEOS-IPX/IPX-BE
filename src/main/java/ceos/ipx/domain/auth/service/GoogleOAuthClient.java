package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.GoogleTokenResponse;
import ceos.ipx.domain.auth.dto.GoogleUserInfoResponse;
import ceos.ipx.global.config.GoogleOAuthProperties;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private final GoogleOAuthProperties googleOAuthProperties;

    public GoogleTokenResponse exchangeCodeForToken(String code) {
        try {
            GoogleTokenResponse response = WebClient.create()
                    .post()
                    .uri(googleOAuthProperties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("code", code)
                            .with("client_id", googleOAuthProperties.clientId())
                            .with("client_secret", googleOAuthProperties.clientSecret())
                            .with("redirect_uri", googleOAuthProperties.redirectUri())
                            .with("grant_type", GRANT_TYPE_AUTHORIZATION_CODE))
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();

            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new BusinessException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
            }

            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
        }
    }

    public GoogleUserInfoResponse getUserInfo(String googleAccessToken) {
        try {
            GoogleUserInfoResponse response = WebClient.create()
                    .get()
                    .uri(googleOAuthProperties.userInfoUri())
                    .headers(headers -> headers.setBearerAuth(googleAccessToken))
                    .retrieve()
                    .bodyToMono(GoogleUserInfoResponse.class)
                    .block();

            if (response == null
                    || response.id() == null || response.id().isBlank()
                    || response.email() == null || response.email().isBlank()) {
                throw new BusinessException(ErrorCode.OAUTH_USER_INFO_FAILED);
            }

            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
    }
}