package ceos.ipx.domain.cases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 구성요소 자동 추출 응답
 *
 * 사용자가 편집 후
 * POST /api/searches의 components 배열에 담아서 최종 저장
 */
@Schema(description = "구성요소 자동 추출 응답")
public record ComponentExtractResponse(

        @Schema(description = "추출된 구성요소 목록 (3~7개)")
        List<ComponentDto> components
) {

    @Schema(description = "구성요소 개별 항목")
    public record ComponentDto(

            @Schema(description = "라벨 (A, B, C, ...)", example = "A")
            String label,

            @Schema(description = "구성요소 명칭", example = "OBD 데이터 수집부")
            String name,

            @Schema(description = "구성요소 상세 설명", example = "차량의 OBD 포트를 통해 실시간으로 센서 데이터를 수집하는 모듈")
            String description
    ) {}
}
