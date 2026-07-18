package ceos.ipx.domain.cases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 검색 중단 응답
 */
@Schema(description = "검색 중단 응답")
public record SearchCancelResponse(

        @Schema(description = "사건 ID", example = "1")
        Long caseId,

        @Schema(description = "실제 취소 여부 (이미 완료된 경우 false)", example = "true")
        Boolean cancelled
) {
}