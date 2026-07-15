package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.domain.cases.entity.InventionComponent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "구성요소 목록 조회 응답")
public record ComponentListResponse(

        @Schema(description = "사건 ID", example = "1")
        Long caseId,

        @Schema(description = "등록된 구성요소 개수", example = "2")
        Integer componentCount,

        @Schema(description = "displayOrder 오름차순으로 정렬된 구성요소 목록")
        List<ComponentDto> components

) {

    public static ComponentListResponse of(
            Long caseId,
            List<InventionComponent> components
    ) {
        List<ComponentDto> componentDtos = components.stream()
                .map(ComponentDto::from)
                .toList();

        return new ComponentListResponse(
                caseId,
                componentDtos.size(),
                componentDtos
        );
    }

    @Schema(description = "구성요소 정보")
    public record ComponentDto(

            @Schema(description = "구성요소 ID", example = "1")
            Long componentId,

            @Schema(description = "구성요소 표시 순서", example = "1")
            Short displayOrder,

            @Schema(description = "표시 순서를 알파벳으로 변환한 화면용 라벨", example = "A")
            String label,

            @Schema(description = "구성요소명", example = "저전력 통신 모듈")
            String name,

            @Schema(
                    description = "구성요소 설명",
                    example = "센서 데이터 전송 시 전력 소모를 줄이는 통신 모듈"
            )
            String description

    ) {

        public static ComponentDto from(InventionComponent component) {
            return new ComponentDto(
                    component.getId(),
                    component.getDisplayOrder(),
                    toLabel(component.getDisplayOrder()),
                    component.getName(),
                    component.getDescription()
            );
        }

        private static String toLabel(Short displayOrder) {
            return String.valueOf((char) ('A' + displayOrder - 1));
        }
    }
}