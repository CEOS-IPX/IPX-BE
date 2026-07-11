package ceos.ipx.domain.cases.service;

import org.springframework.stereotype.Component;

/**
 * rrf_score 기반 relevance 계산
 *
 * 백분위 기준:
 *   - 상위 20% → "매우 높음"
 *   - 20-50% → "높음"
 *   - 50-80% → "보통"
 *   - 그 외    → "낮음"
 */
@Component
public class RelevanceCalculator {

    private static final String VERY_HIGH = "매우 높음";
    private static final String HIGH = "높음";
    private static final String MEDIUM = "보통";
    private static final String LOW = "낮음";

    public String toRelevance(int rank, int total) {
        if (total <= 0) return LOW;

        double percentile = (double) rank / total;
        if (percentile <= 0.2) return VERY_HIGH;
        else if (percentile <= 0.5) return HIGH;
        else if (percentile <= 0.8) return MEDIUM;
        else return LOW;
    }
}