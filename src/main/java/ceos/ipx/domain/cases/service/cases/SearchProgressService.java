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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 검색 진행 상태 Redis 관리 (Spring 담당 부분)
 *
 * Python이 저장한 Redis Hash 구조:
 *   Key: search:{caseId}
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
    public SearchStatusResponse getStatus(Long caseId) {
        String key = KEY_PREFIX + caseId;
        Map<Object, Object> entries = new HashMap<>();

        try{
            entries = redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis 검색 진행 상태 조회 실패: caseId={}", caseId, e);
            throw new BusinessException(ErrorCode.REDIS_ERROR);
        }

        if (entries.isEmpty()) {
            log.warn("[SearchProgress] 검색 세션 없음: caseId={}", caseId);
            throw new BusinessException(ErrorCode.SEARCH_NOT_FOUND);
        }

        return new SearchStatusResponse(
                caseId,
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
    public SearchCancelResponse cancel(Long caseId) {
        String key = KEY_PREFIX + caseId;
        String currentStatus = "";
        try{
            currentStatus = (String) redisTemplate.opsForHash().get(key, "status");
        } catch (Exception e) {
            log.error("Redis 검색 상태 조회 실패: caseId={}", caseId, e);
            throw new BusinessException(ErrorCode.REDIS_ERROR);
        }

        if (currentStatus == null) {
            log.warn("[SearchProgress] 취소 대상 없음: caseId={}", caseId);
            throw new BusinessException(ErrorCode.SEARCH_NOT_FOUND);
        }

        if (TERMINAL_STATUSES.contains(currentStatus)) {
            log.info("[SearchProgress] 이미 종료된 검색: caseId={}, status={}",
                    caseId, currentStatus);
            return new SearchCancelResponse(caseId, false);
        }

        try {
            Map<String, String> progressData = new HashMap<>();
            progressData.put("status", "cancelled");
            progressData.put("step", "선행기술 탐색 취소");
            progressData.put("updated_at", OffsetDateTime.now(ZoneOffset.UTC).toString());

            redisTemplate.opsForHash().putAll(key, progressData);

            log.info("[SearchProgress] 검색 취소 처리: caseId={}", caseId);
            return new SearchCancelResponse(caseId, true);
        } catch (Exception e) {
            log.error("[SearchProgress] Redis 검색 취소 상태 업데이트 실패: caseId={}", caseId, e);
            throw new BusinessException(ErrorCode.REDIS_ERROR);
        }
    }

    /**
     * 프론트 폴링 대비
     * Spring이 Python 호출 전에 Redis에 초기 상태 저장
     */
    public void markStarted(Long caseId) {
        String key = KEY_PREFIX + caseId;
        String now = OffsetDateTime.now(ZoneOffset.UTC).toString();

        try {
            Map<String, String> progressData = new HashMap<>();
            progressData.put("status", "in_progress");
            progressData.put("step", "검색 준비 중");
            progressData.put("progress", "0");
            progressData.put("started_at", now);
            progressData.put("updated_at", now);
            progressData.put("reason_invalid", "");
            progressData.put("error", "");

            redisTemplate.opsForHash().putAll(key, progressData);

            log.info("[SearchProgress] 검색 시작 상태 저장: caseId={}", caseId);
        } catch (Exception e) {
            log.error("[SearchProgress] Redis 검색 시작 상태 업데이트 실패: caseId={}", caseId, e);
            throw new BusinessException(ErrorCode.REDIS_ERROR);
        }
    }

    /**
     * DB 저장 완료 후 Redis 상태를 completed로 변경
     * CaseSearchTxService.saveSearchResults의 afterCommit 훅에서 호출
     */
    public void markCompleted(Long caseId) {
        String key = KEY_PREFIX + caseId;
        int maxRetries = 3;
        long backoffMs = 100;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Map<String, String> progressData = new HashMap<>();
                progressData.put("status", "completed");
                progressData.put("step", "선행기술 탐색 완료");
                progressData.put("progress", "100");
                progressData.put("updated_at", OffsetDateTime.now(ZoneOffset.UTC).toString());

                redisTemplate.opsForHash().putAll(key, progressData);
                log.info("[SearchProgress] 완료 상태 저장: caseId={}, attempt={}", caseId, attempt);
                return;

            } catch (Exception e) {
                log.warn("[SearchProgress] Redis 업데이트 실패 (재시도 {}/{}): caseId={}, error={}",
                        attempt, maxRetries, caseId, e.getMessage());

                if (attempt == maxRetries) {
                    log.error("[SearchProgress][CRITICAL] Redis 업데이트 최종 실패: caseId={}", caseId, e);
                    return;
                }

                try {
                    Thread.sleep(backoffMs * attempt);  // 100ms, 200ms, 300ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * 시스템 오류 발생 시 Redis 상태를 failed로 변경
     * Python 응답 없음, 네트워크 오류, Spring 자체 예외 등에서 호출
     */
    public void markFailed(Long caseId, String errorMessage) {
        String key = KEY_PREFIX + caseId;
        int maxRetries = 3;
        long backoffMs = 100;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {

            try {
                Map<String, String> progressData = new HashMap<>();
                progressData.put("status", "failed");
                progressData.put("step", "시스템 오류");
                progressData.put("updated_at", OffsetDateTime.now(ZoneOffset.UTC).toString());
                progressData.put("error", errorMessage != null ? errorMessage : "알 수 없는 오류");

                redisTemplate.opsForHash().putAll(key, progressData);

                log.info("[SearchProgress] 실패 상태 저장: caseId={}, error={}", caseId, errorMessage);
                return;
            } catch (Exception e) {
                log.warn("[SearchProgress] Redis 실패 상태 업데이트 실패 (재시도 {}/{}): caseId={}, error={}",
                        attempt, maxRetries, caseId, e.getMessage());

                if (attempt == maxRetries) {
                    log.error("[SearchProgress][CRITICAL] Redis 업데이트 최종 실패: caseId={}", caseId, e);
                    return;
                }

                try {
                    Thread.sleep(backoffMs * attempt);  // 100ms, 200ms, 300ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
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