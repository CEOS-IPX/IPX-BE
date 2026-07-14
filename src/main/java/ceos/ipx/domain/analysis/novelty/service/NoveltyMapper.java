package ceos.ipx.domain.analysis.novelty.service;

import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.global.opensearch.dto.PatentDocument;
import ceos.ipx.global.python.dto.request.novelty.PythonPriorArtForAnalysis;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 신규성 분석 관련 매핑 유틸
 */
@Component
public class NoveltyMapper {

    /**
     * PriorArt + OpenSearch 문서로 신규성 분석용 DTO 조립
     * 신규성은 abstract, tech_purpose가 필요 없음 (진보성과 다름)
     */
    public PythonPriorArtForAnalysis toPythonPriorArt(PriorArt priorArt, PatentDocument document) {
        return PythonPriorArtForAnalysis.builder()
                .applicationNumber(priorArt.getApplicationNumber())
                .title(priorArt.getTitle())
                .claimsIndependent(document != null ? document.claimsIndependentAsString() : "")
                .build();
    }

    /**
     * displayOrder(1, 2, 3, ...) → label ("A", "B", "C", ...)
     */
    public String toLabel(short displayOrder) {
        return String.valueOf((char) ('A' + displayOrder - 1));
    }

    /**
     * displayOrder(short) → label ("A", "B", "C", ...)
     */
    public String toLabel(Short displayOrder) {
        return String.valueOf((char) ('A' + displayOrder - 1));
    }

    /**
     * 구성요소 라벨(A, B, C, ...) → InventionComponent 매핑
     *
     * @param components 사건의 구성요소 리스트 (displayOrder 순)
     * @return label을 key로 하는 Map
     */
    public Map<String, InventionComponent> buildLabelToComponentMap(
            List<InventionComponent> components) {
        Map<String, InventionComponent> map = new HashMap<>();
        for (InventionComponent c : components) {
            map.put(toLabel(c.getDisplayOrder()), c);
        }
        return map;
    }
}