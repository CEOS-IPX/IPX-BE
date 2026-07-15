package ceos.ipx.domain.analysis.inventivestep.dto.response;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.cases.entity.PriorArt;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.EnumMap;
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
    // 카테고리별 기본 안내 문구
    private static final Map<String, Object> NUMERICAL_LIMIT_PLACEHOLDER = Map.of(
            "effect_items", List.of(Map.of(
                    "metric", "측정 지표를 입력하세요 (예: 진단 정확도)",
                    "unit", "단위를 입력하세요 (예: %)",
                    "prior_art_value", "종래기술 수치를 입력하세요",
                    "invention_value", "본 발명 수치를 입력하세요",
                    "improvement", "개선률을 입력하세요"
            ))
    );

    private static final Map<String, Object> COMBINATION_MOTIVATION_PLACEHOLDER = Map.of(
            "background_limit", "종래기술의 근본적 한계를 직접 입력하세요",
            "teaching_away", "D1과 D2를 결합할 동기가 없는 이유를 직접 입력하세요"
    );

    private static final Map<String, Object> COMMON_TECHNIQUE_PLACEHOLDER = Map.of(
            "target_label", "",
            "target_name", "",
            "rebuttal", "주지관용기술이 아니라는 반박 논리를 직접 입력하세요"
    );

    private static final Map<String, Object> SIMPLE_DESIGN_PLACEHOLDER = Map.of(
            "changed_component_label", "",
            "changed_component_name", "",
            "non_obviousness", "단순 설계 변경이 아닌 비자명한 개선이라는 논거를 직접 입력하세요"
    );

    /**
     * 카테고리별 기본 안내 문구 반환
     * recommended=false 케이스에서 프론트에 어떤 필드를 채워야 할지 안내
     */
    private static Map<String, Object> placeholderFor(ArgumentType type) {
        return switch (type) {
            case NUMERICAL_LIMIT -> NUMERICAL_LIMIT_PLACEHOLDER;
            case COMBINATION_MOTIVATION -> COMBINATION_MOTIVATION_PLACEHOLDER;
            case COMMON_TECHNIQUE -> COMMON_TECHNIQUE_PLACEHOLDER;
            case SIMPLE_DESIGN -> SIMPLE_DESIGN_PLACEHOLDER;
        };
    }

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
        return new ArgumentDto(type, recommended, recommended ? content : placeholderFor(type));
    }

    /**
     * GET /api/cases/{caseId}/inventive-step
     *
     * DB에서 조회한 4개 argument를 카테고리별로 매핑한 뒤,
     * recommended=false는 placeholder로 대체
     */

    public static InventiveStepResponse ofEntities(
            InventiveStepAnalysis analysis,
            List<InventiveArgument> argumentEntities
    ) {
        // 카테고리 → argument 매핑
        Map<ArgumentType, InventiveArgument> byType = new EnumMap<>(ArgumentType.class);
        for (InventiveArgument arg : argumentEntities) {
            byType.put(arg.getArgumentType(), arg);
        }

        List<ArgumentDto> argumentDtos = List.of(
                fromEntity(ArgumentType.NUMERICAL_LIMIT, byType),
                fromEntity(ArgumentType.COMBINATION_MOTIVATION, byType),
                fromEntity(ArgumentType.COMMON_TECHNIQUE, byType),
                fromEntity(ArgumentType.SIMPLE_DESIGN, byType)
        );

        return new InventiveStepResponse(
                analysis.getId(),
                PriorArtBrief.of(analysis.getPrimaryArt()),
                analysis.getSecondaryArt() != null ? PriorArtBrief.of(analysis.getSecondaryArt()) : null,
                argumentDtos
        );
    }

    /**
     * DB 엔티티 하나를 ArgumentDto로 변환
     * 엔티티가 없거나 recommended=false면 placeholder 사용
     */
    private static ArgumentDto fromEntity(
            ArgumentType type,
            Map<ArgumentType, InventiveArgument> byType
    ) {
        InventiveArgument arg = byType.get(type);
        if (arg == null || !Boolean.TRUE.equals(arg.getRecommended())) {
            return new ArgumentDto(type, false, placeholderFor(type));
        }
        return new ArgumentDto(type, true, arg.getContent());
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
                  { "target_label": "B", "target_name": "...", "rebuttal": "..." }
              - SIMPLE_DESIGN:
                  { "changed_component_label": "C", "changed_component_name": "...", "non_obviousness": "..." }
            
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