package ceos.ipx.domain.cases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선행문헌 삭제 응답")
public record DeletePriorArtResponse(

        @Schema(description = "삭제된 선행문헌 ID", example = "1")
        Long deletedPriorArtId,

        @Schema(description = "삭제된 선행문헌이 속했던 사건 ID", example = "1")
        Long caseId

) {

    public static DeletePriorArtResponse of(
            Long deletedPriorArtId,
            Long caseId
    ) {
        return new DeletePriorArtResponse(
                deletedPriorArtId,
                caseId
        );
    }
}
