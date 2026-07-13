package ceos.ipx.global.python.dto.response.inventivestep;

import java.util.List;

/**
 * Python /analyze/inventive-step/select-categories
 *
 * categories: 선정된 카테고리 문자열 리스트
 *   가능한 값: "numerical_limit", "combination_motivation", "common_technique", "simple_design"
 */
public record PythonSelectCategoriesResponse(
        List<String> categories
) {}