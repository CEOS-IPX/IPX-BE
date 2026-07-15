package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.UpdatePriorArtInclusionRequest;
import ceos.ipx.domain.cases.dto.response.UpdatePriorArtInclusionResponse;
import ceos.ipx.domain.cases.service.priorart.PriorArtService;
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
        name = "선행기술 관리",
        description = "선행문헌 분석 포함 여부 수정 API"
)
@RestController
@RequestMapping("/api/prior-arts")
@RequiredArgsConstructor
public class PriorArtInclusionController {

    private final PriorArtService priorArtService;

    @Operation(
            summary = "선행문헌 분석 포함 여부 수정",
            description = """
                    특정 선행문헌을 신규성·진보성 분석에 포함할지 여부를 수정합니다.

                    - 해당 선행문헌이 로그인한 사용자의 사건에 속한 경우에만 수정할 수 있습니다.
                    - 기존 값과 같은 값을 요청해도 정상 처리합니다.
                    """
    )
    @PatchMapping("/{priorArtId}/inclusion")
    public ResponseEntity<ApiResponse<UpdatePriorArtInclusionResponse>>
    updatePriorArtInclusion(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "선행문헌 ID", example = "1")
            @PathVariable Long priorArtId,

            @Valid
            @RequestBody UpdatePriorArtInclusionRequest request
    ) {
        UpdatePriorArtInclusionResponse response =
                priorArtService.updatePriorArtInclusion(
                        userId,
                        priorArtId,
                        request.included()
                );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}