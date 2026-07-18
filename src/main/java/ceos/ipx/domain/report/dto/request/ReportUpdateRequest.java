package ceos.ipx.domain.report.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

@Schema(description = "분석 리포트 수정 요청")
public record ReportUpdateRequest(

        @Schema(
                description = "수정할 작성 변리사명. null이거나 생략하면 기존 값을 유지합니다.",
                example = "오지송",
                nullable = true
        )
        @Size(
                max = 100,
                message = "작성 변리사명은 100자 이내로 입력해주세요."
        )
        String authorName,

        @Schema(
                description = "수정할 신규성 충족 여부. null이거나 생략하면 기존 값을 유지합니다.",
                example = "true",
                nullable = true
        )
        Boolean noveltySatisfied,

        @Schema(
                description = "수정할 진보성 충족 여부. null이거나 생략하면 기존 값을 유지합니다.",
                example = "false",
                nullable = true
        )
        Boolean inventiveSatisfied,

        @Schema(
                description = "수정할 분석 리포트 종합 결론. null이거나 생략하면 기존 값을 유지합니다.",
                example = "D1과의 차이점이 명확하여 신규성은 인정될 수 있으나, 일부 구성의 결합 가능성은 추가 검토가 필요합니다.",
                nullable = true
        )
        String overallConclusion

) {

    @JsonIgnore
    @AssertTrue(
            message = "수정할 값을 하나 이상 입력해야 하며, 작성 변리사명과 종합 결론은 공백일 수 없습니다."
    )
    public boolean isValidUpdateRequest() {
        boolean hasAnyValue =
                authorName != null
                        || noveltySatisfied != null
                        || inventiveSatisfied != null
                        || overallConclusion != null;

        if (!hasAnyValue) {
            return false;
        }

        if (authorName != null && authorName.isBlank()) {
            return false;
        }

        return overallConclusion == null
                || !overallConclusion.isBlank();
    }
}