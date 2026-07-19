package ceos.ipx.domain.analysis.novelty.dto.request;

import ceos.ipx.domain.analysis.novelty.entity.ComparisonResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "신규성 비교 결과 수정 요청")
public record NoveltyComparisonUpdateRequest(

        @NotNull
        @Schema(
                description = "구성요소 비교 결과",
                example = "SIMILAR",
                allowableValues = {"IDENTICAL", "SIMILAR", "NOVEL"}
        )
        ComparisonResult comparisonResult,

        @Schema(
                description = "선행문헌 인용 부분",
                example = "문단 [0032]에서 센서 데이터를 수집하는 구성이 개시되어 있음",
                nullable = true
        )
        String citation
) {
}