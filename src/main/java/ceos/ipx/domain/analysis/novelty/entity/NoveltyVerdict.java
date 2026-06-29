package ceos.ipx.domain.analysis.novelty.entity;

public enum NoveltyVerdict {
    SAFE,      // 신규성 충족
    RISKY,     // 위험
    BLOCKED    // 단일 문헌에 모든 구성 존재
}
