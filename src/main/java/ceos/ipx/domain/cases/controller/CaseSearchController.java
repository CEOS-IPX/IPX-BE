package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchCancelResponse;
import ceos.ipx.domain.cases.dto.response.SearchStartResponse;
import ceos.ipx.domain.cases.dto.response.SearchStatusResponse;
import ceos.ipx.domain.cases.service.cases.CaseSearchService;
import ceos.ipx.global.aop.ratelimit.RateLimit;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 선행기술 탐색 API
 *
 * 엔드포인트:
 *   - POST /api/searches                     : 선행기술 탐색 실행
 *   - GET  /api/cases/{caseId}/searches/status   : 진행 상태 조회
 *   - POST /api/cases/{caseId}/searches/cancel   : 검색 중단
 */
@Tag(name = "선행기술 탐색", description = "선행기술 탐색 실행 및 진행 상태 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CaseSearchController {

    private final CaseSearchService caseSearchService;

    @Operation(
            summary = "선행기술 탐색 실행",
            description = """
                    사건 생성과 검색 실행을 하나의 API로 처리합니다.

                    - `caseId`가 null이면 새 사건을 생성하고, 값이 있으면 재검색으로 처리
                    - 재검색 시 기존 구성요소, 선행기술, 신규성/진보성 분석, 리포트가 모두 삭제됨
                    - 검색은 백그라운드에서 실행
                    - 응답의 caseId로 진행 상태를 폴링해 완료 여부 확인
                    """
    )
    @RateLimit(apiName = "search")
    @PostMapping("/searches")
    public ResponseEntity<ApiResponse<SearchStartResponse>> startSearch(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SearchRequest request
    ) {
        SearchStartResponse response = caseSearchService.startSearch(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(
            summary = "검색 진행 상태 조회",
            description = """
                    검색 실행 응답으로 받은 caseId로 진행 상태를 조회합니다.
                    프론트는 이 API를 폴링(예: 2초 간격)해서 완료 여부를 확인합니다.

                    - status: `in_progress` | `completed` | `failed` | `cancelled`
                    - completed 감지 시: `GET /api/cases/{caseId}/prior-arts`로 결과 조회
                    """
    )
    @GetMapping("/cases/{caseId}/searches/status")
    public ResponseEntity<ApiResponse<SearchStatusResponse>> getStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId
    ) {
        SearchStatusResponse response = caseSearchService.getStatus(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(
            summary = "검색 중단",
            description = """
                    진행 중인 검색을 중단합니다.
                    이미 완료된 검색은 `cancelled=false`로 응답합니다.
                    """
    )
    @PostMapping("/cases/{caseId}/searches/cancel")
    public ResponseEntity<ApiResponse<SearchCancelResponse>> cancelSearch(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId
    ) {
        SearchCancelResponse response = caseSearchService.cancelSearch(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}