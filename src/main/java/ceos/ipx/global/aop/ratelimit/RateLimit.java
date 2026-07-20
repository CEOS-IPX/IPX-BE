package ceos.ipx.global.aop.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자별 Rate Limit 어노테이션
 *
 * Controller 메서드에 부착하여 시간당 요청 수를 제한한다.
 * 인증된 사용자의 userId 기준으로 제한하며, Redis에 카운터를 저장한다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * API 식별자 (Redis 키로 사용)
     * 예: "search", "novelty", "inventive-step", "component-extract", "manual-add"
     */
    String apiName();
}