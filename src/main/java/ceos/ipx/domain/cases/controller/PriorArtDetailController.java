package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.response.PriorArtDetailResponse;
import ceos.ipx.domain.cases.service.priorart.PriorArtService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "선행기술 상세", description = "선행문헌 상세 조회 API")
@RestController
@RequestMapping("/api/prior-arts")
@RequiredArgsConstructor
public class PriorArtDetailController {

    private final PriorArtService priorArtService;

    @Operation(
            summary = "선행문헌 상세 조회",
            description = """
                    선행문헌 ID로 상세 정보를 조회합니다.

                    - 사건별 분석 정보는 PostgreSQL에서 조회합니다.
                    - 원본 특허 상세 정보는 OpenSearch에서 조회합니다.
                    - 해당 선행문헌이 로그인한 사용자의 사건에 속한 경우에만 조회할 수 있습니다.
                    """
    )
    @GetMapping("/{priorArtId}")
    public ResponseEntity<ApiResponse<PriorArtDetailResponse>> getPriorArtDetail(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "선행문헌 ID", example = "1")
            @PathVariable Long priorArtId
    ) {
        PriorArtDetailResponse response =
                priorArtService.getPriorArtDetail(userId, priorArtId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}