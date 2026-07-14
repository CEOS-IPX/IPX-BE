package ceos.ipx.domain.analysis.novelty.entity;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ComparisonResult {
    IDENTICAL("동일"),
    SIMILAR("유사"),
    NOVEL("신규");

    private final String label;

    ComparisonResult(String label) {
        this.label = label;
    }

    /**
     * Python이 반환하는 한글 라벨 → enum 변환
     */
    public static ComparisonResult fromLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PYTHON_RESPONSE));
    }
}
