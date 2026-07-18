package ceos.ipx.domain.analysis.inventivestep.entity;

import lombok.Getter;

@Getter
public enum ArgumentType {

    NUMERICAL_LIMIT("수치한정"),
    COMBINATION_MOTIVATION("복수인용발명결합"),
    COMMON_TECHNIQUE("주지관용기술"),
    SIMPLE_DESIGN("단순설계변경");

    private final String label;

    ArgumentType(String label) {
        this.label = label;
    }
}