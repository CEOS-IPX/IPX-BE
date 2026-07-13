package ceos.ipx.domain.analysis.inventivestep.dto.response;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.cases.entity.PriorArt;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 진보성 분석 결과 응답
 *
 * arguments는 4개 카테고리 모두 포함:
 *   - recommended=true : AI가 추천 (content 있음)
 *   - recommended=false: 해당 없음 (content 빈 Map)
 */
@Schema(description = "진보성 분석 결과 응답")
public record InventiveStepResponse(

        @Schema(description = "분석 ID", example = "1")
        Long analysisId,

        @Schema(description = "주인용 특허 (D1)")
        PriorArtBrief primaryArt,

        @Schema(description = "부인용 특허 (D2). Python이 자동 선정")
        PriorArtBrief secondaryArt,

        @Schema(description = "4개 카테고리 진보성 논리 (항상 4개, recommended 여부로 구분)")
        List<ArgumentDto> arguments
) {

    public static InventiveStepResponse of(
            InventiveStepAnalysis analysis,
            PriorArt d1,
            PriorArt d2,
            Map<ArgumentType, Map<String, Object>> recommendedContents
    ) {

        List<ArgumentDto> argumentDtos = List.of(
                buildArgumentDto(ArgumentType.NUMERICAL_LIMIT, recommendedContents),
                buildArgumentDto(ArgumentType.COMBINATION_MOTIVATION, recommendedContents),
                buildArgumentDto(ArgumentType.COMMON_TECHNIQUE, recommendedContents),
                buildArgumentDto(ArgumentType.SIMPLE_DESIGN, recommendedContents)
        );

        return new InventiveStepResponse(
                analysis.getId(),
                PriorArtBrief.of(d1),
                d2 != null ? PriorArtBrief.of(d2) : null,
                argumentDtos
        );
    }

    private static ArgumentDto buildArgumentDto(
            ArgumentType type,
            Map<ArgumentType, Map<String, Object>> recommendedContents
    ) {
        Map<String, Object> content = recommendedContents.get(type);
        boolean recommended = content != null;
        return new ArgumentDto(type, recommended, recommended ? content : Map.of());
    }


    @Schema(description = "선행기술 간략 정보")
    public record PriorArtBrief(

            @Schema(description = "출원번호", example = "1020170102293")
            String applicationNumber,

            @Schema(description = "발명 명칭", example = "딥러닝 기반 자동차 진단 방법")
            String title,

            @Schema(description = "출원인", example = "(주)다이매틱스")
            String applicantName,

            @Schema(description = "출원일", example = "2017-08-11")
            LocalDate applicationDate
    ) {
        public static PriorArtBrief of(PriorArt priorArt) {
            return new PriorArtBrief(
                    priorArt.getApplicationNumber(),
                    priorArt.getTitle(),
                    priorArt.getApplicantName(),
                    priorArt.getApplicationDate()
            );
        }
    }

    @Schema(description = """
            진보성 논리 개별 항목.
            
            recommended=true 일 때 content 구조 (카테고리별 상이):
              - NUMERICAL_LIMIT:
                  { "effect_items": [{"metric": "...", "unit": "...",
                                      "prior_art_value": "...", "invention_value": "...",
                                      "improvement": "..."}] }
              - COMBINATION_MOTIVATION:
                  { "background_limit": "...", "teaching_away": "..." }
              - COMMON_TECHNIQUE:
                  { "target_component": "B", "rebuttal": "..." }
              - SIMPLE_DESIGN:
                  { "changed_component": "C", "non_obviousness": "..." }
            
            recommended=false 일 때 content는 빈 Map.
            """)
    public record ArgumentDto(

            @Schema(
                    description = "논리 카테고리",
                    example = "NUMERICAL_LIMIT",
                    allowableValues = {
                            "NUMERICAL_LIMIT",
                            "COMBINATION_MOTIVATION",
                            "COMMON_TECHNIQUE",
                            "SIMPLE_DESIGN"
                    }
            )
            ArgumentType argumentType,

            @Schema(
                    description = "AI 추천 여부. true면 이 사건에 유효한 논리, false면 해당 없음",
                    example = "true"
            )
            Boolean recommended,

            @Schema(description = "카테고리별 구조화된 논리 내용 (recommended=false면 빈 Map)")
            Map<String, Object> content
    ) {}
}