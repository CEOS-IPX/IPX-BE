package ceos.ipx.domain.cases.service.cases;

import ceos.ipx.domain.cases.dto.response.SearchCancelResponse;
import ceos.ipx.domain.cases.dto.response.SearchStatusResponse;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

/**
 * 검색 진행 상태 Redis 관리 (Spring 담당 부분)
 *
 * Python이 저장한 Redis Hash 구조:
 *   Key: search:{searchId}
 *   Fields:
 *     - status: in_progress | completed | no_results | invalid_input | failed | cancelled
 *     - step: 현재 단계 설명
 *     - progress: 진행률 (0~100, 문자열)
 *     - started_at: ISO 8601
 *     - updated_at: ISO 8601
 *     - reason_invalid: invalid_input 시 사유
 *     - error: failed 시 에러 메시지
 *
 * 책임 분담:
 *   Python: 진행률 갱신, invalid_input, no_results, 파이프라인 자체 실패 시 failed
 *   Spring: 상태 조회 (getStatus), 취소 (cancel), DB 저장 완료 시 completed, 시스템 오류 시 failed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchProgressService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "search:";

    /** 이미 종료되어 취소 불가능한 상태들 */
    private static final Set<String> TERMINAL_STATUSES = Set.of(
            "completed", "failed", "cancelled", "invalid_input", "no_results"
    );

    /**
     * 검색 진행 상태 조회
     * Redis에 키가 없으면 SEARCH_NOT_FOUND 예외
     */
    public SearchStatusResponse getStatus(String searchId) {
        String key = KEY_PREFIX + searchId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            log.warn("[SearchProgress] 검색 세션 없음: searchId={}", searchId);
            throw new BusinessException(ErrorCode.SEARCH_NOT_FOUND);
        }

        return new SearchStatusResponse(
                searchId,
                asString(entries.get("status")),
                asString(entries.get("step")),
                asInt(entries.get("progress")),
                nullIfEmpty(asString(entries.get("error"))),
                nullIfEmpty(asString(entries.get("reason_invalid")))
        );
    }

    /**
     * 진행 중인 검색을 취소
     *   세션이 없으면 SEARCH_NOT_FOUND 예외
     *   이미 종료된 검색은 cancelled=false 반환 (200 OK)
     *   진행 중인 검색만 status=cancelled로 갱신하고 cancelled=true 반환
     */
    public SearchCancelResponse cancel(String searchId) {
        String key = KEY_PREFIX + searchId;
        String currentStatus = (String) redisTemplate.opsForHash().get(key, "status");

        if (currentStatus == null) {
            log.warn("[SearchProgress] 취소 대상 없음: searchId={}", searchId);
            throw new BusinessException(ErrorCode.SEARCH_NOT_FOUND);
        }

        if (TERMINAL_STATUSES.contains(currentStatus)) {
            log.info("[SearchProgress] 이미 종료된 검색: searchId={}, status={}",
                    searchId, currentStatus);
            return new SearchCancelResponse(searchId, false);
        }

        // in_progress 상태에서만 취소 처리
        String now = OffsetDateTime.now(ZoneOffset.UTC).toString();
        redisTemplate.opsForHash().put(key, "status", "cancelled");
        redisTemplate.opsForHash().put(key, "step", "취소됨");
        redisTemplate.opsForHash().put(key, "updated_at", now);

        log.info("[SearchProgress] 검색 취소 처리: searchId={}", searchId);
        return new SearchCancelResponse(searchId, true);
    }

    /**
     * DB 저장 완료 후 Redis 상태를 completed로 변경
     * CaseSearchTxService.saveSearchResults의 afterCommit 훅에서 호출
     */
    public void markCompleted(String searchId) {
        try {
            String key = KEY_PREFIX + searchId;
            String now = OffsetDateTime.now(ZoneOffset.UTC).toString();

            redisTemplate.opsForHash().put(key, "status", "completed");
            redisTemplate.opsForHash().put(key, "step", "완료");
            redisTemplate.opsForHash().put(key, "progress", "100");
            redisTemplate.opsForHash().put(key, "updated_at", now);

            log.info("[SearchProgress] 완료 상태 저장: searchId={}", searchId);
        } catch (Exception e) {
            log.error("[SearchProgress] Redis 완료 상태 업데이트 실패: searchId={}", searchId, e);
        }
    }

    /**
     * 시스템 오류 발생 시 Redis 상태를 failed로 변경
     * Python 응답 없음, 네트워크 오류, Spring 자체 예외 등에서 호출
     */
    public void markFailed(String searchId, String errorMessage) {
        try {
            String key = KEY_PREFIX + searchId;
            String now = OffsetDateTime.now(ZoneOffset.UTC).toString();

            redisTemplate.opsForHash().put(key, "status", "failed");
            redisTemplate.opsForHash().put(key, "step", "시스템 오류");
            redisTemplate.opsForHash().put(key, "updated_at", now);
            redisTemplate.opsForHash().put(key, "error",
                    errorMessage != null ? errorMessage : "알 수 없는 오류");

            log.info("[SearchProgress] 실패 상태 저장: searchId={}, error={}", searchId, errorMessage);
        } catch (Exception e) {
            log.error("[SearchProgress] Redis 실패 상태 업데이트 실패: searchId={}", searchId, e);
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInt(Object value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String nullIfEmpty(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}