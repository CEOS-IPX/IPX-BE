package ceos.ipx.domain.analysis.inventivestep.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "진보성 논리 분석 수정 요청")
public record InventiveArgumentUpdateRequest(

        @Schema(
                description = "해당 진보성 논리를 선택하여 최종 판단에 적용할지 여부. 전달하지 않으면 기존 값을 유지합니다.",
                example = "true",
                nullable = true
        )
        Boolean recommended,

        @Schema(
                description = "논리 유형별 구조화된 분석 내용. 전달하지 않으면 기존 값을 유지하며, 빈 객체를 전달하면 내용을 비웁니다.",
                example = """
                        {
                          "reason": "두 문헌의 기술 분야가 달라 결합 동기가 약하다고 판단됩니다."
                        }
                        """,
                nullable = true
        )
        Map<String, Object> content
) {

    public boolean hasNoUpdates() {
        return recommended == null && content == null;
    }
}
