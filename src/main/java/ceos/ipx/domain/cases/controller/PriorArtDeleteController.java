package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.response.DeletePriorArtResponse;
import ceos.ipx.domain.cases.service.priorart.PriorArtService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "선행기술 관리",
        description = "선행문헌 삭제 API"
)
@RestController
@RequestMapping("/api/prior-arts")
@RequiredArgsConstructor
public class PriorArtDeleteController {

    private final PriorArtService priorArtService;

    @Operation(
            summary = "선행문헌 삭제",
            description = """
                    사건에 저장된 선행문헌을 삭제합니다.

                    - 로그인한 사용자의 사건에 속한 선행문헌만 삭제할 수 있습니다.
                    - 특허 원본 데이터와 OpenSearch 문서는 삭제하지 않습니다.
                    - 삭제 대상이 신규성 또는 진보성 분석에서 사용 중인 경우,
                      관련 분석 결과와 하위 데이터가 함께 삭제될 수 있습니다.
                    - 관련 분석이 삭제되면 사건 완료 상태와 기존 리포트도 무효화됩니다.
                    """
    )
    @DeleteMapping("/{priorArtId}")
    public ResponseEntity<ApiResponse<DeletePriorArtResponse>> deletePriorArt(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "삭제할 선행문헌 ID", example = "1")
            @PathVariable Long priorArtId
    ) {
        DeletePriorArtResponse response =
                priorArtService.deletePriorArt(userId, priorArtId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
