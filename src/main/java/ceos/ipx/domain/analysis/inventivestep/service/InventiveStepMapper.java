package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.global.opensearch.dto.PatentDocument;
import ceos.ipx.global.python.dto.response.inventivestep.PythonCombinationMotivationResult;
import ceos.ipx.global.python.dto.response.inventivestep.PythonCommonTechniqueResult;
import ceos.ipx.global.python.dto.response.inventivestep.PythonNumericalLimitResult;
import ceos.ipx.global.python.dto.response.inventivestep.PythonSimpleDesignResult;
import ceos.ipx.global.python.dto.shared.PythonInventionComponent;
import ceos.ipx.global.python.dto.shared.PythonPriorArtInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 진보성 분석 관련 매핑 유틸
 *
 * Spring 엔티티 + OpenSearch 문서 → Python 요청 DTO 변환
 */
@Component
public class InventiveStepMapper {

    /**
     * PriorArt + OpenSearch 문서로 PythonPriorArtInfo 조립
     *
     * PriorArt에는 서지/요약 정보,
     * PatentDocument에는 청구항/초록 원문
     */
    public PythonPriorArtInfo toPythonPriorArtInfo(PriorArt priorArt, PatentDocument document) {
        return PythonPriorArtInfo.builder()
                .applicationNumber(priorArt.getApplicationNumber())
                .title(priorArt.getTitle())
                .abstractText(document != null ? document.abstractClean() : null)
                .claimsIndependent(document != null ? document.claimsIndependentAsString() : "")
                .techPurpose(priorArt.getTechPurpose())
                .build();
    }

    /**
     * InventionComponent 엔티티 리스트를 Python DTO 리스트로 변환
     * displayOrder를 label(A, B, C, ...)로 변환
     */
    public List<PythonInventionComponent> toPythonComponents(List<InventionComponent> components) {
        return components.stream()
                .map(c -> PythonInventionComponent.builder()
                        .label(toLabel(c.getDisplayOrder()))
                        .name(c.getName())
                        .description(c.getDescription())
                        .build())
                .toList();
    }

    /**
     * displayOrder(1, 2, 3, ...) → label ("A", "B", "C", ...)
     */
    private String toLabel(short displayOrder) {
        return String.valueOf((char) ('A' + displayOrder - 1));
    }

    // ============================================================
    // Python 응답 → JSONB Map 변환
    // ============================================================

    /**
     * NumericalLimitResult → Map<String, Object> (JSONB 저장용)
     */
    public Map<String, Object> toContentMap(PythonNumericalLimitResult result) {
        return Map.of("effect_items", result.effectItems());
    }

    /**
     * CombinationMotivationResult → Map
     */
    public Map<String, Object> toContentMap(PythonCombinationMotivationResult result) {
        return Map.of(
                "background_limit", result.backgroundLimit(),
                "teaching_away", result.teachingAway()
        );
    }

    /**
     * CommonTechniqueResult → Map
     */
    public Map<String, Object> toContentMap(PythonCommonTechniqueResult result) {
        return Map.of(
                "target_component", result.targetComponent(),
                "rebuttal", result.rebuttal()
        );
    }

    /**
     * SimpleDesignResult → Map
     */
    public Map<String, Object> toContentMap(PythonSimpleDesignResult result) {
        return Map.of(
                "changed_component", result.changedComponent(),
                "non_obviousness", result.nonObviousness()
        );
    }
}
