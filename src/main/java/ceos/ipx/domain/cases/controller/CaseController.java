package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.CaseSortType;
import ceos.ipx.domain.cases.dto.request.CaseStatusGroup;
import ceos.ipx.domain.cases.dto.request.CaseUpdateRequest;
import ceos.ipx.domain.cases.dto.response.CaseDeleteResponse;
import ceos.ipx.domain.cases.dto.response.CaseDetailResponse;
import ceos.ipx.domain.cases.dto.response.CaseListResponse;
import ceos.ipx.domain.cases.dto.response.CaseUpdateResponse;
import ceos.ipx.domain.cases.dto.response.RecentCaseListResponse;
import ceos.ipx.domain.cases.service.CaseService;
import ceos.ipx.domain.report.dto.request.ReportCreateRequest;
import ceos.ipx.domain.report.dto.response.ReportCreateResponse;
import ceos.ipx.domain.report.service.ReportSaveResult;
import ceos.ipx.domain.report.service.ReportService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Case", description = "사건 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;
    private final ReportService reportService;

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
        return ApiResponse.ok(
                caseService.getRecentCases(userId, limit)
        );
    }

    @Operation(
            summary = "사건 수정",
            description = "로그인한 사용자가 자신이 생성한 사건의 사건명, 사명, 의뢰인을 수정합니다."
    )
    @PatchMapping("/{caseId}")
    public ApiResponse<CaseUpdateResponse> updateCase(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId,
            @Valid @RequestBody CaseUpdateRequest request
    ) {
        return ApiResponse.ok(
                caseService.updateCase(userId, caseId, request)
        );
    }

    @Operation(
            summary = "사건 삭제",
            description = "로그인한 사용자가 자신이 생성한 사건과 연관 데이터를 삭제합니다."
    )
    @DeleteMapping("/{caseId}")
    public ApiResponse<CaseDeleteResponse> deleteCase(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(
                caseService.deleteCase(userId, caseId)
        );
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
        return ApiResponse.ok(
                caseService.getCaseDetail(userId, caseId)
        );
    }

    @Operation(
            summary = "분석 리포트 생성 및 덮어쓰기",
            description = """
                    신규성 분석과 진보성 분석이 모두 존재하는 사건의 분석 리포트를 저장합니다.
                    기존 리포트가 있는 경우 overwrite가 true여야 덮어쓸 수 있습니다.
                    """
    )
    @PostMapping("/{caseId}/report")
    public ResponseEntity<ApiResponse<ReportCreateResponse>> saveReport(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        ReportSaveResult result =
                reportService.saveReport(userId, caseId, request);

        if (result.created()) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(result.response()));
        }

        return ResponseEntity.ok(
                ApiResponse.ok(result.response())
        );
    }
}