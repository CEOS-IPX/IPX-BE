package ceos.ipx.domain.auth.service;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GoogleOAuthStateService {

    private static final String GOOGLE_OAUTH_STATE_KEY_PREFIX = "oauth:google:state:";
    private static final Duration GOOGLE_OAUTH_STATE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;

    public void saveState(String state, String frontendRedirectUri) {
        String key = createKey(state);

        stringRedisTemplate.opsForValue()
                .set(key, frontendRedirectUri, GOOGLE_OAUTH_STATE_TTL);
    }

    public String consumeState(String state) {
        if (state == null || state.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
        }

        String key = createKey(state);
        String frontendRedirectUri = stringRedisTemplate.opsForValue().get(key);

        if (frontendRedirectUri == null || frontendRedirectUri.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
        }

        stringRedisTemplate.delete(key);

        return frontendRedirectUri;
    }

    private String createKey(String state) {
        return GOOGLE_OAUTH_STATE_KEY_PREFIX + state;
    }
}