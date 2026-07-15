package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;

import java.util.Map;

/**
 * Python 카테고리 문자열 ↔ ArgumentType enum 변환 유틸
 *
 * Python은 snake_case ("numerical_limit"),
 * Spring은 UPPER_SNAKE_CASE (NUMERICAL_LIMIT).
 */
public final class ArgumentTypeConverter {

    private static final Map<String, ArgumentType> PYTHON_TO_ENUM = Map.of(
            "numerical_limit", ArgumentType.NUMERICAL_LIMIT,
            "combination_motivation", ArgumentType.COMBINATION_MOTIVATION,
            "common_technique", ArgumentType.COMMON_TECHNIQUE,
            "simple_design", ArgumentType.SIMPLE_DESIGN
    );

    private ArgumentTypeConverter() {}

    public static ArgumentType fromPython(String pythonValue) {
        if (pythonValue == null) return null;
        return PYTHON_TO_ENUM.get(pythonValue.toLowerCase());
    }
}
