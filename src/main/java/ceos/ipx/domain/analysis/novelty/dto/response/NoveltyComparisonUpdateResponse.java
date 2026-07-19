package ceos.ipx.domain.analysis.novelty.dto.response;

import ceos.ipx.domain.analysis.novelty.entity.ComparisonResult;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신규성 비교 결과 수정 응답")
public record NoveltyComparisonUpdateResponse(

        @Schema(description = "신규성 비교 결과 ID", example = "1")
        Long comparisonId,

        @Schema(
                description = "구성요소 비교 결과",
                example = "SIMILAR",
                allowableValues = {"IDENTICAL", "SIMILAR", "NOVEL"}
        )
        ComparisonResult comparisonResult,

        @Schema(description = "구성요소 비교 결과 한글 라벨", example = "유사")
        String comparisonResultLabel,

        @Schema(
                description = "선행문헌 인용 부분",
                example = "문단 [0032]에서 센서 데이터를 수집하는 구성이 개시되어 있음",
                nullable = true
        )
        String citation
) {

    public static NoveltyComparisonUpdateResponse from(
            NoveltyComparison comparison
    ) {
        return new NoveltyComparisonUpdateResponse(
                comparison.getId(),
                comparison.getComparisonResult(),
                comparison.getComparisonResult().getLabel(),
                comparison.getCitation()
        );
    }
}