package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.GoogleUserInfoResponse;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthSignupTokenService {

    private static final String OAUTH_SIGNUP_TOKEN_KEY_PREFIX = "oauth:google:signup:";
    private static final Duration OAUTH_SIGNUP_TOKEN_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public String saveGoogleUserInfo(GoogleUserInfoResponse googleUserInfo) {
        String token = UUID.randomUUID().toString();
        String key = createKey(token);

        try {
            String value = objectMapper.writeValueAsString(googleUserInfo);

            stringRedisTemplate.opsForValue()
                    .set(key, value, OAUTH_SIGNUP_TOKEN_TTL);

            return token;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public GoogleUserInfoResponse getGoogleUserInfo(String token) {
        String key = createKey(token);
        String value = stringRedisTemplate.opsForValue().get(key);

        if (value == null) {
            throw new BusinessException(ErrorCode.OAUTH_SIGNUP_TOKEN_EXPIRED);
        }

        try {
            return objectMapper.readValue(value, GoogleUserInfoResponse.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteGoogleUserInfo(String token) {
        String key = createKey(token);
        stringRedisTemplate.delete(key);
    }

    private String createKey(String token) {
        return OAUTH_SIGNUP_TOKEN_KEY_PREFIX + token;
    }
}