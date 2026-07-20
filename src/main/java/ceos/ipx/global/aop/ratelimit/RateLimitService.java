package ceos.ipx.global.aop.ratelimit;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 사용자별 Rate Limit Redis 기반 구현
 *
 * Redis 키 구조: {@code rate:{apiName}:{userId}:{hourBucket}}
 *   hourBucket: 매 시간 자정부터의 경과 시간 (h 단위)
 *   값: 해당 시간 동안의 요청 횟수
 *   TTL: 1시간
 *
 * 제한 방식: 매 시간 정각에 카운터 초기화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final long HOUR_IN_MILLIS = 60L * 60L * 1000L;

    /**
     * 사용자별 시간당 요청 제한 검사.
     * 제한 초과 시 예외 발생.
     *
     * @param userId 사용자 ID
     * @param apiName API 식별자 (예: "search")
     * @param limit 시간당 허용 요청 수
     * @throws BusinessException {@code RATE_LIMIT_EXCEEDED} 제한 초과 시
     */
    public void checkLimit(Long userId, String apiName, int limit) {
        long hourBucket = System.currentTimeMillis() / HOUR_IN_MILLIS;
        String key = String.format("rate:%s:%d:%d", apiName, userId, hourBucket);

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count == null) {
                log.error("[RateLimit] increment 응답 null: key={}", key);
                return;  // 안전하게 통과
            }

            if (count == 1L) {
                redisTemplate.expire(key, Duration.ofHours(1));
            }

            if (count > limit) {
                log.warn("[RateLimit] 초과: userId={}, apiName={}, count={}, limit={}",
                        userId, apiName, count, limit);
                throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
            }

            log.debug("[RateLimit] 통과: userId={}, apiName={}, count={}/{}",
                    userId, apiName, count, limit);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 접근 실패 시 요청 허용
            // Redis 장애로 서비스 전체 마비되는 것 방지
            log.error("[RateLimit] Redis 접근 실패, 요청 허용: userId={}, apiName={}",
                    userId, apiName, e);
        }
    }
}