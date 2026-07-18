package ceos.ipx.domain.cases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 검색 진행 상태 응답
 */
@Schema(description = "검색 진행 상태 응답")
public record SearchStatusResponse(

        @Schema(description = "사건 ID", example = "1")
        Long caseId,

        @Schema(description = "진행 상태", example = "in_progress",
                allowableValues = {"in_progress", "completed", "failed", "cancelled"})
        String status,

        @Schema(description = "현재 단계 설명", example = "검색 의도 분석 중")
        String step,

        @Schema(description = "진행률 (0~100)", example = "45", minimum = "0", maximum = "100")
        Integer progress,

        @Schema(description = "의도 해석 실패 이유", example = "발명의 명칭과 핵심 기술 설명을 구체적으로 입력해 주세요.")
        String reasonInvalid,

        @Schema(description = "실패 시 에러 메시지", example = "null", nullable = true)
        String error
) {
}