package ceos.ipx.domain.cases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "구성요소 목록 저장 및 수정 요청")
public record ComponentSaveRequest(

        @Schema(
                description = "저장할 전체 구성요소 목록. 빈 배열 전달 시 기존 구성요소를 모두 삭제합니다."
        )
        @NotNull
        @Size(max = 32767)
        List<@Valid ComponentDto> components

) {

    @Schema(description = "저장할 구성요소 정보")
    public record ComponentDto(

            @Schema(
                    description = "구성요소명",
                    example = "저전력 통신 모듈",
                    maxLength = 255
            )
            @NotBlank
            @Size(max = 255)
            String name,

            @Schema(
                    description = "구성요소 설명",
                    example = "센서 데이터 전송 시 전력 소모를 줄이는 통신 모듈"
            )
            @NotBlank
            String description

    ) {
    }
}