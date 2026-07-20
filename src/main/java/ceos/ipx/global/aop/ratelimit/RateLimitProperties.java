package ceos.ipx.global.aop.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * application.yml의 rate limit 설정 매핑
 *
 * yml 구조:
 *  ratelimit:
 *    search: 20
 *    component-extract: 30
 *    manual-add: 30
 *    inventive-step: 10
 *    novelty: 10
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    /**
     * apiName → 시간당 허용 요청 수 매핑
     * yml에 정의된 모든 rate limit 설정이 여기 담긴다
     */
    private Map<String, Integer> limits = new HashMap<>();

    /**
     * apiName에 해당하는 제한값 조회
     * yml에 정의 안 되어 있으면 null 반환
     */
    public Integer getLimit(String apiName) {
        return limits.get(apiName);
    }
}
