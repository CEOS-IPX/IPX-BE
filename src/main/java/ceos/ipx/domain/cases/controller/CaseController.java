package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.CaseSortType;
import ceos.ipx.domain.cases.dto.request.CaseStatusGroup;
import ceos.ipx.domain.cases.dto.request.CaseUpdateRequest;
import ceos.ipx.domain.cases.dto.response.CaseDeleteResponse;
import ceos.ipx.domain.cases.dto.response.CaseDetailResponse;
import ceos.ipx.domain.cases.dto.response.CaseListResponse;
import ceos.ipx.domain.cases.dto.response.CaseUpdateResponse;
import ceos.ipx.domain.cases.dto.response.ComponentListResponse;
import ceos.ipx.domain.cases.dto.response.RecentCaseListResponse;
import ceos.ipx.domain.cases.service.CaseService;
import ceos.ipx.domain.cases.service.component.ComponentService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ceos.ipx.domain.cases.dto.request.ComponentSaveRequest;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Case", description = "사건 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;
    private final ComponentService componentService;

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
            summary = "구성요소 목록 조회",
            description = """
                    특정 사건에 저장된 발명 구성요소 목록을 조회합니다.

                    - 구성요소는 displayOrder 오름차순으로 반환됩니다.
                    - label은 displayOrder를 기반으로 계산됩니다.
                    - 등록된 구성요소가 없는 경우 빈 목록을 반환합니다.
                    - 다른 사용자의 사건에 접근하면 CA002를 반환합니다.
                    """
    )
    @GetMapping("/{caseId}/components")
    public ApiResponse<ComponentListResponse> getComponents(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(
                componentService.getComponents(userId, caseId)
        );
    }

    @Operation(
            summary = "구성요소 목록 저장 및 수정",
            description = """
                    특정 사건의 발명 구성요소 전체 목록을 저장합니다.

                    - 요청 배열의 순서대로 displayOrder를 1부터 부여합니다.
                    - label은 DB에 저장하지 않고 displayOrder를 기반으로 응답에서 계산합니다.
                    - 동일한 순서의 기존 구성요소는 수정합니다.
                    - 새롭게 추가된 순서는 신규 구성요소로 저장합니다.
                    - 요청에서 제외된 기존 구성요소는 삭제합니다.
                    - 빈 components 배열을 전달하면 기존 구성요소를 모두 삭제합니다.
                    - 다른 사용자의 사건에 접근하면 CA002를 반환합니다.
                    """
    )
    @PutMapping("/{caseId}/components")
    public ApiResponse<ComponentListResponse> saveComponents(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long caseId,
            @Valid @RequestBody ComponentSaveRequest request
    ) {
        return ApiResponse.ok(
                componentService.saveComponents(
                        userId,
                        caseId,
                        request
                )
        );
    }
}