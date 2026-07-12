package ceos.ipx.global.python.dto.response;

import java.util.List;

/**
 * Python /components/extract 응답 DTO
 */
public record PythonComponentExtractResponse(
        List<Component> components
) {

    public record Component(
            String name,
            String description
    ) {}
}