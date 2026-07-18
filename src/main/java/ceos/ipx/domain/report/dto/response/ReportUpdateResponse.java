package ceos.ipx.domain.report.dto.response;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "분석 리포트 수정 응답")
public class ReportUpdateResponse {

    @Schema(description = "리포트 ID", example = "1")
    private Long reportId;

    @Schema(description = "사건 ID", example = "1")
    private Long caseId;

    @Schema(description = "수정 후 작성 변리사명", example = "오지송")
    private String authorName;

    @Schema(description = "수정 후 신규성 충족 여부", example = "true")
    private Boolean noveltySatisfied;

    @Schema(description = "수정 후 진보성 충족 여부", example = "false")
    private Boolean inventiveSatisfied;

    @Schema(
            description = "수정 후 분석 리포트 종합 결론",
            example = "D1과의 차이점이 명확하여 신규성은 인정될 수 있으나, 일부 구성의 결합 가능성은 추가 검토가 필요합니다."
    )
    private String overallConclusion;

    @Schema(
            description = "리포트 최종 수정일시",
            example = "2026-07-07T15:30:00"
    )
    private LocalDateTime updatedAt;

    public static ReportUpdateResponse of(
            Report report,
            Case caseEntity
    ) {
        return ReportUpdateResponse.builder()
                .reportId(report.getId())
                .caseId(caseEntity.getId())
                .authorName(report.getAuthorName())
                .noveltySatisfied(report.getNoveltySatisfied())
                .inventiveSatisfied(report.getInventiveSatisfied())
                .overallConclusion(report.getOverallConclusion())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}