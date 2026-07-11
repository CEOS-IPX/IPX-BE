package ceos.ipx.domain.cases.dto.response;

import java.util.List;

/**
 * 구성요소 자동 추출 응답
 */
public record ComponentExtractResponse(
        List<ComponentDto> components
) {

    public record ComponentDto(
            String label,        // A, B, C, ...
            String name,
            String description
    ) {}
}
