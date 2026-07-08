package ceos.ipx.domain.analysis.novelty.entity;

import lombok.Getter;

@Getter
public enum ComparisonResult {
    IDENTICAL("동일"),
    SIMILAR("유사"),
    NOVEL("신규");

    private final String label;

    ComparisonResult(String label) {
        this.label = label;
    }
}
