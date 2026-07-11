package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.AddManualRequest;
import ceos.ipx.domain.cases.dto.response.PriorArtResponse;
import ceos.ipx.domain.cases.service.PriorArtService;
import ceos.ipx.global.response.ApiResponse;
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
@RestController
@RequestMapping("/api/cases/{caseId}/prior-arts")
@RequiredArgsConstructor
public class PriorArtController {

    private final PriorArtService priorArtService;

    /**
     * 사건의 선행기술 목록 조회
     * rrf_score DESC + created_at ASC 정렬, relevance는 순위 기반 계산
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PriorArtResponse>>> getPriorArts(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId
    ) {
        List<PriorArtResponse> results = priorArtService.getPriorArts(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * 출원번호로 선행기술 수동 추가:
     *   - 중복 특허는 자동 필터링
     *   - Python 호출로 서지 정보 조회 + LLM 요약
     *   - PriorArt INSERT (source=MANUAL)
     *
     * 응답: 추가 후 전체 prior_arts 목록 (기존 + 새로 추가된 것)
     */
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<List<PriorArtResponse>>> addManual(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId,
            @RequestBody @Valid AddManualRequest request
    ) {
        List<PriorArtResponse> results = priorArtService.addManual(userId, caseId, request);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}