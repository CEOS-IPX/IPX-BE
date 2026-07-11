package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchStartResponse;
import ceos.ipx.domain.cases.service.CaseSearchService;
import ceos.ipx.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 선행기술 탐색 API.
 *
 * 담당 엔드포인트:
 *   - POST /api/searches              : 선행기술 탐색 실행 (사건 생성 + 검색 시작)
 *   - GET  /api/searches/{id}/status  : 진행 상태 조회 (Step 3)
 *   - POST /api/searches/{id}/cancel  : 검색 중단 (Step 3)
 */
@RestController
@RequestMapping("/api/searches")
@RequiredArgsConstructor
public class CaseSearchController {

    private final CaseSearchService caseSearchService;

    @PostMapping
    public ResponseEntity<ApiResponse<SearchStartResponse>> startSearch(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SearchRequest request
    ) {
        SearchStartResponse response = caseSearchService.startSearch(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
