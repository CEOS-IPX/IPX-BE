package ceos.ipx.global.python.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python /search/{search_id}/status 응답.
 *
 * Python progress_tracker.get_status() 반환 구조:
 *   - search_id: 검색 세션 ID
 *   - status: "in_progress" | "completed" | "failed" | "cancelled"
 *   - step: 현재 단계 설명 (예: "특허 데이터베이스 검색 중")
 *   - progress: 진행률 (0~100)
 *   - started_at: 검색 시작 시각 (ISO 8601)
 *   - updated_at: 마지막 갱신 시각 (ISO 8601)
 *   - error: 실패 시 에러 메시지 (Optional)
 *
 * search_id가 Redis에 없으면 Python이 null 반환 → Spring에서 404 처리.
 */
public record PythonSearchStatusResponse(
        @JsonProperty("search_id")
        String searchId,

        String status,

        String step,

        Integer progress,

        @JsonProperty("started_at")
        String startedAt,

        @JsonProperty("updated_at")
        String updatedAt,

        String error
) {}