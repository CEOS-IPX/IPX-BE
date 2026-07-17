package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.global.python.dto.response.search.PythonSearchStatusResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 검색 진행 상태 응답
 *
 * Python 응답(snake_case)을 camelCase로 변환해 프론트에 전달
 */
@Schema(description = "검색 진행 상태 응답")
public record SearchStatusResponse(

        @Schema(description = "검색 세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String searchId,

        @Schema(description = "진행 상태", example = "in_progress",
                allowableValues = {"in_progress", "completed", "failed", "cancelled"})
        String status,

        @Schema(description = "현재 단계 설명", example = "검색 의도 분석 중")
        String step,

        @Schema(description = "진행률 (0~100)", example = "45", minimum = "0", maximum = "100")
        Integer progress
) {
    public static SearchStatusResponse from(PythonSearchStatusResponse response) {
        return new SearchStatusResponse(
                response.searchId(),
                response.status(),
                response.step(),
                response.progress()
        );
    }
}