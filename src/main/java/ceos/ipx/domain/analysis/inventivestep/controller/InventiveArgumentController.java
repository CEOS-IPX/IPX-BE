package ceos.ipx.domain.analysis.inventivestep.controller;

import ceos.ipx.domain.analysis.inventivestep.dto.request.InventiveArgumentUpdateRequest;
import ceos.ipx.domain.analysis.inventivestep.dto.response.InventiveArgumentUpdateResponse;
import ceos.ipx.domain.analysis.inventivestep.service.InventiveArgumentService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "진보성 논리 분석",
        description = "진보성 분석의 논리별 결과 수정 API"
)
@RestController
@RequestMapping("/api/inventive-arguments")
@RequiredArgsConstructor
public class InventiveArgumentController {

    private final InventiveArgumentService inventiveArgumentService;

    @Operation(
            summary = "진보성 논리 분석 수정",
            description = """
                    특정 진보성 논리의 최종 적용 여부와 구조화된 분석 내용을 부분 수정합니다.

                    처리 기준:
                    - argumentId로 진보성 논리 분석 조회
                    - 해당 논리가 속한 사건의 소유권 검증
                    - content 수정
                    - argumentType은 수정하지 않음
                    - 전달되지 않은 필드는 기존 값 유지
                    - content에 빈 객체를 전달하면 내용을 비움
                    - 수정할 필드가 하나도 없으면 400 반환
                    - 진보성 분석 전체를 다시 실행하지 않음
                    - Python AI 서버와 OpenSearch를 호출하지 않음
                    - 사건의 진보성 분석 완료 시각을 변경하지 않음
                    """
    )
    @PatchMapping("/{argumentId}")
    public ResponseEntity<ApiResponse<InventiveArgumentUpdateResponse>> update(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId,

            @Parameter(
                    description = "진보성 논리 분석 ID",
                    example = "1"
            )
            @PathVariable Long argumentId,

            @RequestBody
            @Valid
            InventiveArgumentUpdateRequest request
    ) {
        InventiveArgumentUpdateResponse response =
                inventiveArgumentService.update(userId, argumentId, request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
