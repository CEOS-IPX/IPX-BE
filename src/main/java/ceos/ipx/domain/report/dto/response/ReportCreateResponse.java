package ceos.ipx.domain.report.dto.response;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "분석 리포트 생성 및 덮어쓰기 응답")
public class ReportCreateResponse {

    @Schema(description = "리포트 ID", example = "1")
    private Long reportId;

    @Schema(description = "사건 ID", example = "1")
    private Long caseId;

    @Schema(description = "작성 변리사명", example = "오지송")
    private String authorName;

    @Schema(description = "신규성 최종 충족 여부", example = "true")
    private Boolean noveltySatisfied;

    @Schema(description = "진보성 최종 충족 여부", example = "false")
    private Boolean inventiveSatisfied;

    @Schema(
            description = "종합 결론",
            example = "일부 구성요소는 선행문헌과 유사하나, 진보성 주장이 가능할 것으로 판단됩니다."
    )
    private String overallConclusion;

    @Schema(description = "사건의 리포트 생성 완료 시각", example = "2026-07-07T15:00:00")
    private LocalDateTime reportCompletedAt;

    @Schema(description = "리포트 최초 생성일시", example = "2026-07-07T15:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "리포트 최종 수정일시", example = "2026-07-07T15:00:00")
    private LocalDateTime updatedAt;

    public static ReportCreateResponse of(Report report, Case caseEntity) {
        return ReportCreateResponse.builder()
                .reportId(report.getId())
                .caseId(caseEntity.getId())
                .authorName(report.getAuthorName())
                .noveltySatisfied(report.getNoveltySatisfied())
                .inventiveSatisfied(report.getInventiveSatisfied())
                .overallConclusion(report.getOverallConclusion())
                .reportCompletedAt(caseEntity.getReportCompletedAt())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}