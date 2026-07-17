package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.global.python.dto.response.search.PythonCancelResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 검색 중단 응답
 *
 * Python 응답(snake_case)을 camelCase로 변환해 프론트에 전달
 */
@Schema(description = "검색 중단 응답")
public record SearchCancelResponse(

        @Schema(description = "검색 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String searchId,

        @Schema(description = "실제 취소 여부 (이미 완료된 경우 false)", example = "true")
        Boolean cancelled
) {
    public static SearchCancelResponse from(PythonCancelResponse response) {
        return new SearchCancelResponse(response.searchId(), response.cancelled());
    }
}