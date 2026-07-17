package ceos.ipx.domain.cases.dto.response;

/**
 * 관련도 등급
 *
 * rrf_score 순위 백분위 기준으로 RelevanceCalculator가 산정
 */
public enum Relevance {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW
}