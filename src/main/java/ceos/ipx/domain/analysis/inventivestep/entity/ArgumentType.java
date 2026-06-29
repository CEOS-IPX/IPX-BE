package ceos.ipx.domain.analysis.inventivestep.entity;

public enum ArgumentType {
    NUMERICAL_LIMIT,            // 수치한정 - 효과의 현저성
    COMBINATION_MOTIVATION,     // 복수인용발명결합 - Teaching Away
    COMMON_TECHNIQUE,           // 주지관용기술 - 반박 논리
    SIMPLE_DESIGN               // 단순설계변경 - 비자명성 논리
}