package ceos.ipx.domain.cases.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddManualRequest(
        @NotNull
        @NotEmpty
        List<@NotNull String> applicationNumbers
) {}