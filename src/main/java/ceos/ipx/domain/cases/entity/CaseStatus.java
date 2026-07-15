package ceos.ipx.domain.cases.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseStatus {

    NOT_STARTED("시작 전"),
    SEARCH_COMPLETED("선행기술 조사 완료"),
    NOVELTY_COMPLETED("신규성 분석 완료"),
    INVENTIVE_COMPLETED("진보성 분석 완료"),
    REPORT_COMPLETED("리포트 생성 완료");

    private final String label;
}