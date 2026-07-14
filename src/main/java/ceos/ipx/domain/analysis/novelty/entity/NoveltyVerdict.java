package ceos.ipx.domain.analysis.novelty.entity;

import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum NoveltyVerdict {
    VERY_HIGH("매우 높음"),
    HIGH("높음"),
    MEDIUM("보통"),
    LOW("낮음");

    private final String label;

    NoveltyVerdict(String label) {
        this.label = label;
    }

    /**
     * Python이 반환하는 한글 라벨 → enum 변환
     */
    public static NoveltyVerdict fromLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PYTHON_RESPONSE));
    }
}
