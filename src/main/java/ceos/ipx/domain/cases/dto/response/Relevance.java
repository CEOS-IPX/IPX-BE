package ceos.ipx.domain.cases.dto.response;

/**
 * 관련도 등급
 *
 * rrf_score LLM을 통해 계산되어 RelevanceCalculator가 산정
 */
public enum Relevance {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW
}