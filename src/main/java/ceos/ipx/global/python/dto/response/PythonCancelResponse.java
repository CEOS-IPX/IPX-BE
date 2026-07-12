package ceos.ipx.global.python.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Python /search/{search_id}/cancel 응답
 *
 * Python 측 CancelResponse:
 *   - search_id: 취소된 검색 ID
 *   - cancelled: 실제로 취소가 발생했는지 (이미 완료 상태였으면 false)
 */
@Schema(description = "검색 중단 응답")
public record PythonCancelResponse(

        @Schema(description = "검색 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @JsonProperty("search_id")
        String searchId,

        @Schema(description = "실제 취소 여부 (이미 완료된 경우 false)", example = "true")
        Boolean cancelled
) {}