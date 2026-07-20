package ceos.ipx.global.aop.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * RateLimit 어노테이션이 부착된 메서드를 인터셉트하여 요청 수를 제한
 *
 * 동작 흐름:
 *   SecurityContext에서 인증된 사용자의 userId 추출
 *   RateLimitService의 checkLimit으로 제한 검사
 *   초과 시 예외 발생
 *
 * 미인증 요청은 이 Aspect가 처리하지 않음 (Security Filter에서 이미 401)
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        Long userId = extractUserId();

        if (userId == null) {
            log.warn("[RateLimit] 인증 정보 없이 API 호출: apiName={}", rateLimit.apiName());
            return joinPoint.proceed();
        }

        Integer limit = rateLimitProperties.getLimit(rateLimit.apiName());

        if (limit == null) {
            log.error("[RateLimit] yml에 apiName 미정의, 요청 허용: apiName={}. " +
                            "application.yml의 ratelimit.limits에 추가 필요.",
                    rateLimit.apiName());

            return joinPoint.proceed();
        }

        rateLimitService.checkLimit(userId, rateLimit.apiName(), limit);

        return joinPoint.proceed();
    }

    /**
     * SecurityContext에서 userId 추출
     * {@code @AuthenticationPrincipal Long userId} 방식과 동일한 principal 사용
     */
    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }

        return null;
    }
}
