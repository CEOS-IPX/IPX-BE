package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.domain.cases.entity.PriorArt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선행문헌 분석 포함 여부 수정 응답")
public record UpdatePriorArtInclusionResponse(

        @Schema(description = "선행문헌 ID", example = "1")
        Long priorArtId,

        @Schema(description = "소속 사건 ID", example = "1")
        Long caseId,

        @Schema(description = "신규성·진보성 분석 포함 여부", example = "false")
        boolean included

) {

    public static UpdatePriorArtInclusionResponse from(PriorArt priorArt) {
        return new UpdatePriorArtInclusionResponse(
                priorArt.getId(),
                priorArt.getCaseEntity().getId(),
                priorArt.isIncluded()
        );
    }
}