package ceos.ipx.domain.analysis.inventivestep.dto.response;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "진보성 논리 분석 수정 응답")
public record InventiveArgumentUpdateResponse(

        @Schema(
                description = "진보성 논리 분석 ID",
                example = "1"
        )
        Long argumentId,

        @Schema(
                description = "진보성 논리 유형",
                example = "COMBINATION_MOTIVATION",
                allowableValues = {
                        "NUMERICAL_LIMIT",
                        "COMBINATION_MOTIVATION",
                        "COMMON_TECHNIQUE",
                        "SIMPLE_DESIGN"
                }
        )
        ArgumentType argumentType,

        @Schema(
                description = "해당 진보성 논리의 AI 추천 여부",
                example = "true"
        )
        Boolean recommended,

        @Schema(
                description = "논리 유형별 구조화된 분석 내용",
                example = """
                        {
                          "reason": "두 문헌의 기술 분야가 달라 결합 동기가 약합니다."
                        }
                        """
        )
        Map<String, Object> content
) {

    public static InventiveArgumentUpdateResponse from(
            InventiveArgument argument
    ) {
        return new InventiveArgumentUpdateResponse(
                argument.getId(),
                argument.getArgumentType(),
                argument.getRecommended(),
                argument.getContent()
        );
    }
}
