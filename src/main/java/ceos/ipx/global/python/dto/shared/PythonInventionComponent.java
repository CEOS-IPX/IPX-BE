package ceos.ipx.global.python.dto.shared;

import lombok.Builder;

/**
 * Python으로 전달하는 발명 구성요소 정보
 *
 * Python 측 InventionComponent와 필드명 일치
 */
@Builder
public record PythonInventionComponent(
        String label,
        String name,
        String description
) {}