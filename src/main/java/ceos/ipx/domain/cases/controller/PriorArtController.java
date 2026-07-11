package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.response.PriorArtResponse;
import ceos.ipx.domain.cases.service.PriorArtService;
import ceos.ipx.global.response.ApiResponse;
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
 *   - POST /api/cases/{caseId}/prior-arts/manual : 선행기술 탐색 후, 수동 추가
 */
@RestController
@RequestMapping("/api/cases/{caseId}/prior-arts")
@RequiredArgsConstructor
public class PriorArtController {

    private final PriorArtService priorArtService;

    /**
     * 사건의 선행기술 목록 조회
     * rrf_score DESC 정렬, relevance는 순위 기반 계산
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PriorArtResponse>>> getPriorArts(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId
    ) {
        List<PriorArtResponse> results = priorArtService.getPriorArts(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}