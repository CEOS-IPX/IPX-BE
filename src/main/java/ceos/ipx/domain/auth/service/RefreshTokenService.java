package ceos.ipx.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";

    private final StringRedisTemplate stringRedisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken, long expirationSeconds) {
        String key = createKey(userId);

        stringRedisTemplate.opsForValue()
                .set(key, refreshToken, Duration.ofSeconds(expirationSeconds));
    }

    public String getRefreshToken(Long userId) {
        String key = createKey(userId);

        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean matches(Long userId, String refreshToken) {
        String savedRefreshToken = getRefreshToken(userId);

        return savedRefreshToken != null && savedRefreshToken.equals(refreshToken);
    }

    public void deleteRefreshToken(Long userId) {
        String key = createKey(userId);

        stringRedisTemplate.delete(key);
    }

    private String createKey(Long userId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }
}