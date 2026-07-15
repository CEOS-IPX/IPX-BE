package ceos.ipx.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

    private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "blacklist:accessToken:";

    private final StringRedisTemplate stringRedisTemplate;

    public void blacklist(String accessToken, long remainingExpirationMillis) {
        if (remainingExpirationMillis <= 0) {
            return;
        }

        String key = createKey(accessToken);

        stringRedisTemplate.opsForValue()
                .set(key, "logout", Duration.ofMillis(remainingExpirationMillis));
    }

    public boolean isBlacklisted(String accessToken) {
        String key = createKey(accessToken);

        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    private String createKey(String accessToken) {
        return ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + accessToken;
    }
}