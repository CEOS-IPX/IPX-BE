package ceos.ipx.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "분석 리포트 생성 및 덮어쓰기 요청")
public record ReportCreateRequest(

        @Schema(
                description = "작성 변리사명",
                example = "오지송",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "작성 변리사명은 필수입니다.")
        @Size(max = 100, message = "작성 변리사명은 100자 이내로 입력해주세요.")
        String authorName,

        @Schema(
                description = "사용자가 최종 판단한 신규성 충족 여부",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "신규성 충족 여부는 필수입니다.")
        Boolean noveltySatisfied,

        @Schema(
                description = "사용자가 최종 판단한 진보성 충족 여부",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "진보성 충족 여부는 필수입니다.")
        Boolean inventiveSatisfied,

        @Schema(
                description = "분석 리포트 종합 결론",
                example = "일부 구성요소는 선행문헌과 유사하나, 결합 동기가 부족하여 진보성 주장이 가능할 것으로 판단됩니다.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "종합 결론은 필수입니다.")
        String overallConclusion,

        @Schema(
                description = "기존 리포트 덮어쓰기 여부. 생략하거나 null이면 false로 처리",
                example = "false",
                nullable = true
        )
        Boolean overwrite

) {
}