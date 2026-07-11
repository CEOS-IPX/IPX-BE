package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchStartResponse;
import ceos.ipx.domain.cases.service.CaseSearchService;
import ceos.ipx.global.python.dto.response.PythonCancelResponse;
import ceos.ipx.global.python.dto.response.PythonSearchStatusResponse;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "선행기술 탐색", description = "선행기술 탐색 실행 및 진행 상태 관리 API")
@RestController
@RequestMapping("/api/searches")
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
                    - 응답의 searchId로 진행 상태를 폴링해 완료 여부 확인
                    """
    )
    @PostMapping
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
                    검색 실행 응답으로 받은 searchId로 진행 상태를 조회합니다.
                    프론트는 이 API를 폴링(예: 2초 간격)해서 완료 여부를 확인합니다.

                    - status: `in_progress` | `completed` | `failed` | `cancelled`
                    - completed 감지 시: `GET /api/cases/{caseId}/prior-arts`로 결과 조회
                    """
    )
    @GetMapping("/{searchId}/status")
    public ResponseEntity<ApiResponse<PythonSearchStatusResponse>> getStatus(
            @Parameter(description = "검색 실행 응답으로 받은 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String searchId
    ) {
        PythonSearchStatusResponse response = caseSearchService.getStatus(searchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(
            summary = "검색 중단",
            description = """
                    진행 중인 검색을 중단합니다.
                    이미 완료된 검색은 `cancelled=false`로 응답합니다.
                    """
    )
    @PostMapping("/{searchId}/cancel")
    public ResponseEntity<ApiResponse<PythonCancelResponse>> cancelSearch(
            @Parameter(description = "검색 세션 UUID")
            @PathVariable String searchId
    ) {
        PythonCancelResponse response = caseSearchService.cancelSearch(searchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}