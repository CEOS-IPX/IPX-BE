package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.AddManualRequest;
import ceos.ipx.domain.cases.dto.response.PriorArtListResponse;
import ceos.ipx.domain.cases.dto.response.PriorArtResponse;
import ceos.ipx.domain.cases.service.priorart.PriorArtService;
import ceos.ipx.global.aop.ratelimit.RateLimit;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 선행기술 조회/관리 API
 *
 * 엔드포인트:
 *   - GET  /api/cases/{caseId}/prior-arts        : 선행기술 조회
 *   - POST /api/cases/{caseId}/prior-arts/manual : 출원번호로 수동 추가
 */
@Tag(name = "선행기술 결과", description = "사건별 선행기술 조회 및 수동 추가 API")
@RestController
@RequestMapping("/api/cases/{caseId}/prior-arts")
@RequiredArgsConstructor
public class PriorArtController {

    private final PriorArtService priorArtService;

    @Operation(
            summary = "선행기술 목록 조회",
            description = """
                    특정 사건의 선행기술 결과를 조회합니다.

                    - 정렬: rrf_score DESC → created_at ASC
                    - relevance는 LLM을 통해 계산됩니다.
                    - 상위 20%: VERY_HIGH
                    - 20% 초과 40% 이하: HIGH
                    - 40% 초과 60% 이하: MEDIUM
                    - 60% 초과 80% 이하: LOW
                    - 80% 초과: VERY_LOW
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PriorArtListResponse>> getPriorArts(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId
    ) {
        List<PriorArtResponse> results = priorArtService.getPriorArts(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(PriorArtListResponse.of(results)));
    }

    @Operation(
            summary = "선행기술 수동 추가",
            description = """
                    출원번호로 선행기술을 직접 추가합니다.

                    - 이미 존재하는 특허는 자동 필터링
                    - Python 호출로 서지 정보 조회 + LLM 요약
                    - 응답: 추가 후 전체 prior_arts 목록 (기존 + 새로 추가된 것)
                    """
    )
    @RateLimit(apiName = "manual-add")
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<PriorArtListResponse>> addManual(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId,
            @RequestBody @Valid AddManualRequest request
    ) {
        List<PriorArtResponse> results = priorArtService.addManual(userId, caseId, request);
        return ResponseEntity.ok(ApiResponse.ok(PriorArtListResponse.of(results)));
    }
}