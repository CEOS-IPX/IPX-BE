package ceos.ipx.global.python.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Python /search/{search_id}/status 응답
 *
 * Python progress_tracker.get_status() 반환 구조
 *   - search_id: 검색 세션 ID
 *   - status: "in_progress" | "completed" | "failed" | "cancelled"
 *   - step: 현재 단계 설명 (예: "검색 의도 분석 중")
 *   - progress: 진행률 (0~100)
 *   - started_at: 검색 시작 시각 (ISO 8601)
 *   - updated_at: 마지막 갱신 시각 (ISO 8601)
 *   - error: 실패 시 에러 메시지 (Optional)
 *
 * search_id가 Redis에 없으면 Python이 null 반환 → Spring에서 404 처리
 */
@Schema(description = "검색 진행 상태 응답")
public record PythonSearchStatusResponse(

        @Schema(description = "검색 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @JsonProperty("search_id")
        String searchId,

        @Schema(description = "진행 상태", example = "in_progress",
                allowableValues = {"in_progress", "completed", "failed", "cancelled"})
        String status,

        @Schema(description = "현재 단계 설명", example = "검색 의도 분석 중")
        String step,

        @Schema(description = "진행률 (0~100)", example = "45", minimum = "0", maximum = "100")
        Integer progress,

        @Schema(description = "검색 시작 시각 (ISO 8601, UTC)", example = "2026-07-10T09:00:00+00:00")
        @JsonProperty("started_at")
        String startedAt,

        @Schema(description = "마지막 갱신 시각 (ISO 8601, UTC)", example = "2026-07-10T09:00:15+00:00")
        @JsonProperty("updated_at")
        String updatedAt,

        @Schema(description = "실패 시 에러 메시지", example = "null", nullable = true)
        String error
) {}