package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.response.CaseDetailResponse;
import ceos.ipx.domain.cases.service.CaseService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Case", description = "사건 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

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