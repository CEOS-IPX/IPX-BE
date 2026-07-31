package ceos.ipx.domain.cases.service.priorart;

import ceos.ipx.domain.cases.dto.response.Relevance;
import org.springframework.stereotype.Component;

/**
 * rrf_score 기반 relevance 계산
 *
 * LLM 점수 기준:
 *   - 상위 20% → VERY_HIGH
 *   - 40% → HIGH
 *   - 60% → MEDIUM
 *   - 80% → LOW
 *   - 그 외 → VERY_LOW
 */
@Component
public class RelevanceCalculator {

    // LLM 점수 기반 매핑
    public Relevance toRelevance(int score) {
        if (score >= 80) return Relevance.VERY_HIGH;
        if (score >= 60) return Relevance.HIGH;
        if (score >= 40) return Relevance.MEDIUM;
        if (score >= 20) return Relevance.LOW;
        return Relevance.VERY_LOW;
    }
}