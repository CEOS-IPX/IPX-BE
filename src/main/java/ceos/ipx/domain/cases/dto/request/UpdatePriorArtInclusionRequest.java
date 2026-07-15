package ceos.ipx.domain.cases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "선행문헌 분석 포함 여부 수정 요청")
public record UpdatePriorArtInclusionRequest(

        @Schema(
                description = "신규성·진보성 분석 포함 여부",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "잘못된 입력값입니다.")
        Boolean included

) {
}