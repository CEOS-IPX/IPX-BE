package ceos.ipx.domain.cases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 검색 시작 즉시 응답
 *
 * 검색 실행은 백그라운드에서 진행 (@Async)
 * 프론트는 이 응답의 searchId로 폴링해서 진행 상태 조회
 * 완료되면 caseId로 GET /prior-arts 호출
 */
@Schema(description = "선행기술 탐색 시작 응답")
public record SearchStartResponse(

        @Schema(description = "검색 세션 ID (진행 상태 폴링에 사용)", example = "550e8400-e29b-41d4-a716-446655440000")
        String searchId,

        @Schema(description = "생성된(또는 재사용된) 사건 ID", example = "1")
        Long caseId,

        @Schema(description = "검색 시작 상태 표시 (항상 \"started\")", example = "started")
        String status
) {
    public static SearchStartResponse of(String searchId, Long caseId) {
        return new SearchStartResponse(searchId, caseId, "started");
    }
}
