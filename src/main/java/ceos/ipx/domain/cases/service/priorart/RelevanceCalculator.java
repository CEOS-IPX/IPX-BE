package ceos.ipx.domain.cases.service.priorart;

import ceos.ipx.domain.cases.dto.response.Relevance;
import org.springframework.stereotype.Component;

/**
 * rrf_score 기반 relevance 계산
 *
 * 백분위 기준:
 *   - 상위 20% → VERY_HIGH
 *   - 20-50% → HIGH
 *   - 50-80% → MEDIUM
 *   - 그 외    → LOW
 */
@Component
public class RelevanceCalculator {

    public Relevance toRelevance(int rank, int total) {
        if (total <= 0) return Relevance.LOW;

        double percentile = (double) rank / total;
        if (percentile <= 0.2) return Relevance.VERY_HIGH;
        else if (percentile <= 0.5) return Relevance.HIGH;
        else if (percentile <= 0.8) return Relevance.MEDIUM;
        else return Relevance.LOW;
    }
}