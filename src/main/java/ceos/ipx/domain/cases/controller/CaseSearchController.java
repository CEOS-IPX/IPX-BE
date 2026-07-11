package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchStartResponse;
import ceos.ipx.domain.cases.service.CaseSearchService;
import ceos.ipx.global.python.dto.response.PythonCancelResponse;
import ceos.ipx.global.python.dto.response.PythonSearchStatusResponse;
import ceos.ipx.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 선행기술 탐색 API
 *
 * 엔드포인트:
 *   - POST /api/searches                     : 선행기술 탐색 실행
 *   - GET  /api/searches/{searchId}/status   : 진행 상태 조회
 *   - POST /api/searches/{searchId}/cancel   : 검색 중단
 */
@RestController
@RequestMapping("/api/searches")
@RequiredArgsConstructor
public class CaseSearchController {

    private final CaseSearchService caseSearchService;

    /**
     * 선행기술 탐색 실행 (사건 생성 + 검색 시작)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SearchStartResponse>> startSearch(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SearchRequest request
    ) {
        SearchStartResponse response = caseSearchService.startSearch(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 검색 진행 상태 조회 (Python 프록시)
     * 프론트가 폴링으로 반복 호출
     *
     * searchId는 UUID (122비트 랜덤)이므로 별도 소유권 검증 X
     */
    @GetMapping("/{searchId}/status")
    public ResponseEntity<ApiResponse<PythonSearchStatusResponse>> getStatus(
            @PathVariable String searchId
    ) {
        PythonSearchStatusResponse response = caseSearchService.getStatus(searchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 검색 중단 (Python 프록시)
     */
    @PostMapping("/{searchId}/cancel")
    public ResponseEntity<ApiResponse<PythonCancelResponse>> cancelSearch(
            @PathVariable String searchId
    ) {
        PythonCancelResponse response = caseSearchService.cancelSearch(searchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}