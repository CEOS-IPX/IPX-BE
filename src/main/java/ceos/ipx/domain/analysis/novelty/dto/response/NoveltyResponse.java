package ceos.ipx.domain.analysis.novelty.dto.response;

import ceos.ipx.domain.analysis.novelty.entity.ComparisonResult;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyVerdict;
import ceos.ipx.domain.analysis.novelty.service.NoveltyMapper;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.global.python.dto.response.novelty.PythonComponentComparison;
import ceos.ipx.global.python.dto.response.novelty.PythonNoveltyResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 신규성 분석 결과 응답
 *
 * POST(실행)와 GET(조회) 모두 동일한 구조로 반환
 * - POST: Python 응답 + 저장된 엔티티로 조립 (ofPython)
 * - GET:  저장된 엔티티에서 재구성 (ofEntities)
 */
@Schema(description = "신규성 분석 결과")
public record NoveltyResponse(

        @Schema(description = "분석 ID", example = "1")
        Long analysisId,

        @Schema(description = "주인용 특허 (D1). Python이 상위 3건 중 가장 유사한 것으로 선정")
        PriorArtBrief primaryArt,

        @Schema(description = "전체 유사도 판단", example = "HIGH",
                allowableValues = {"VERY_HIGH", "HIGH", "MEDIUM", "LOW"})
        NoveltyVerdict overallSimilarity,

        @Schema(description = "신규성 판단 결론 문구", example = "본 발명은 D1과 상당 부분 유사하나 일부 구성요소에서 신규성이 인정될 여지가 있음")
        String conclusionText,

        @Schema(description = "구성요소별 대비 결과 (label A, B, C 순)")
        List<ComparisonDto> comparisons
) {

    /**
     * Python 응답과 저장된 엔티티로 응답 조립
     * POST /api/cases/{caseId}/novelty 실행 직후 사용
     */
    public static NoveltyResponse ofPython(
            NoveltyAnalysis analysis,
            PriorArt d1,
            PythonNoveltyResponse pythonResponse,
            Map<String, InventionComponent> labelToComponentMap
    ) {
        List<ComparisonDto> comparisonDtos = pythonResponse.componentResults() == null
                ? List.of()
                : pythonResponse.componentResults().stream()
                .filter(c -> labelToComponentMap.containsKey(c.componentLabel()))
                .sorted(Comparator.comparing(PythonComponentComparison::componentLabel))
                .map(c -> ComparisonDto.ofPython(c, labelToComponentMap.get(c.componentLabel())))
                .toList();

        return new NoveltyResponse(
                analysis.getId(),
                PriorArtBrief.of(d1),
                analysis.getOverallSimilarity(),
                analysis.getConclusionText(),
                comparisonDtos
        );
    }

    /**
     * 저장된 엔티티로부터 응답 조립
     * GET /api/cases/{caseId}/novelty 조회 시 사용
     */
    public static NoveltyResponse ofEntities(
            NoveltyAnalysis analysis,
            List<NoveltyComparison> comparisons,
            NoveltyMapper mapper
    ) {
        List<ComparisonDto> comparisonDtos = comparisons.stream()
                .sorted(Comparator.comparing(c -> c.getComponent().getDisplayOrder()))
                .map(c -> ComparisonDto.ofEntity(c, mapper))
                .toList();

        return new NoveltyResponse(
                analysis.getId(),
                PriorArtBrief.of(analysis.getD1PriorArt()),
                analysis.getOverallSimilarity(),
                analysis.getConclusionText(),
                comparisonDtos
        );
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
            LocalDate applicationDate,

            @Schema(description = "등록상태", example = "등록")
            String legalStatus
    ) {
        public static PriorArtBrief of(PriorArt priorArt) {
            return new PriorArtBrief(
                    priorArt.getApplicationNumber(),
                    priorArt.getTitle(),
                    priorArt.getApplicantName(),
                    priorArt.getApplicationDate(),
                    priorArt.getLegalStatus()
            );
        }
    }

    @Schema(description = "구성요소 1개에 대한 대비 결과")
    public record ComparisonDto(

            @Schema(description = "구성요소 라벨", example = "A")
            String componentLabel,

            @Schema(description = "구성요소 명칭", example = "센서 데이터 수집부")
            String componentName,

            @Schema(description = "대비 결과", example = "IDENTICAL",
                    allowableValues = {"IDENTICAL", "SIMILAR", "NOVEL"})
            ComparisonResult comparisonResult,

            @Schema(description = "선행기술의 대응 개시 내용",
                    example = "선행기술 청구항 1에 실질적으로 동일한 센서 모듈이 개시됨")
            String disclosureText,

            @Schema(description = "원문 인용 (개시되지 않은 경우 null)",
                    example = "청구항 1: 차량 센서로부터 데이터를 수집하는 모듈...", nullable = true)
            String citation
    ) {

        public static ComparisonDto ofPython(PythonComponentComparison pyComp, InventionComponent component) {
            return new ComparisonDto(
                    pyComp.componentLabel(),
                    component.getName(),
                    ComparisonResult.fromLabel(pyComp.result()),
                    pyComp.disclosureText(),
                    pyComp.citation()
            );
        }

        public static ComparisonDto ofEntity(NoveltyComparison comparison, NoveltyMapper mapper) {
            InventionComponent component = comparison.getComponent();
            return new ComparisonDto(
                    mapper.toLabel(component.getDisplayOrder()),
                    component.getName(),
                    comparison.getComparisonResult(),
                    comparison.getDisclosureText(),
                    comparison.getCitation()
            );
        }
    }
}