package ceos.ipx.global.python.dto.response;

import java.util.Arrays;

public enum SearchStatus {
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String value;

    SearchStatus(String value) {
        this.value = value;
    }

    public static SearchStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(s -> s.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + value));
    }
}
