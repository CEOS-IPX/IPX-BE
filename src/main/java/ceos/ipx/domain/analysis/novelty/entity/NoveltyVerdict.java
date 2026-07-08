package ceos.ipx.domain.analysis.novelty.entity;

import lombok.Getter;

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
}
