package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.CaseSortType;
import ceos.ipx.domain.cases.dto.request.CaseStatusGroup;
import ceos.ipx.domain.cases.dto.response.CaseDetailResponse;
import ceos.ipx.domain.cases.dto.response.CaseListResponse;
import ceos.ipx.domain.cases.dto.response.RecentCaseListResponse;
import ceos.ipx.domain.cases.service.CaseService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Case", description = "사건 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    @Operation(
            summary = "사건 목록 조회",
            description = "로그인한 사용자가 생성한 사건 목록을 검색, 필터링, 정렬 및 페이지 단위로 조회합니다."
    )
    @GetMapping
    public ApiResponse<CaseListResponse> getCaseList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "ALL") CaseStatusGroup statusGroup,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") CaseSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                caseService.getCaseList(
                        userId,
                        statusGroup,
                        keyword,
                        sort,
                        page,
                        size
                )
        );
    }

    @Operation(
            summary = "최근 사건 목록 조회",
            description = "로그인한 사용자의 최근 사건 목록을 수정 시각 기준으로 조회합니다."
    )
    @GetMapping("/recent")
    public ApiResponse<RecentCaseListResponse> getRecentCases(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.ok(caseService.getRecentCases(userId, limit));
    }

    @Operation(
            summary = "사건 상세 조회",
            description = "특정 사건의 기본 정보, 진행 상태, 단계별 완료 시각 및 연관 데이터 개수를 조회합니다."
    )
    @GetMapping("/{caseId}")
    public ApiResponse<CaseDetailResponse> getCaseDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(caseService.getCaseDetail(userId, caseId));
    }
}