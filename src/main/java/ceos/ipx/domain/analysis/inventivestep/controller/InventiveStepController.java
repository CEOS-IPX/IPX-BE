package ceos.ipx.domain.analysis.inventivestep.controller;

import ceos.ipx.domain.analysis.inventivestep.dto.request.InventiveStepRequest;
import ceos.ipx.domain.analysis.inventivestep.dto.response.InventiveStepResponse;
import ceos.ipx.domain.analysis.inventivestep.service.InventiveStepService;
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
 * 진보성 분석 API
 *
 * 엔드포인트:
 *   - POST /api/cases/{caseId}/inventive-step : 진보성 분석 실행
 *   - GET  /api/cases/{caseId}/inventive-step : 저장된 분석 결과 조회
 */
@Tag(name = "진보성 분석", description = "진보성 분석 실행 API")
@RestController
@RequestMapping("/api/cases/{caseId}/inventive-step-analysis")
@RequiredArgsConstructor
public class InventiveStepController {

    private final InventiveStepService inventiveStepService;

    @Operation(
            summary = "진보성 분석 실행",
            description = """
                    사용자가 선택한 주인용(D1)을 기준으로 진보성 분석을 자동 실행합니다.

                    처리 흐름:
                    1. D2(부인용) 자동 선정 (Python LLM)
                    2. 4개 진보성 논리 중 이슈가 될 만한 카테고리 선정 (Python LLM)
                    3. 선정된 카테고리별로 논리 자동 생성 (병렬 실행)
                    4. DB 저장 (기존 진보성 분석이 있으면 삭제 후 새로 생성)

                    응답에 포함되는 카테고리:
                    - NUMERICAL_LIMIT (수치한정)
                    - COMBINATION_MOTIVATION (복수인용발명결합, Teaching Away)
                    - COMMON_TECHNIQUE (주지관용기술 반박)
                    - SIMPLE_DESIGN (단순설계변경 반박)

                    선정된 카테고리만 응답에 포함. 4개 모두 오지 않을 수 있음
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<InventiveStepResponse>> analyze(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId,
            @RequestBody @Valid InventiveStepRequest request
    ) {
        InventiveStepResponse response = inventiveStepService.analyze(
                userId, caseId, request.primaryApplicationNumber()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(
            summary = "진보성 분석 결과 조회",
            description = """
                    이전에 실행된 진보성 분석 결과를 조회합니다.
 
                    응답 구조는 POST와 동일:
                    - 4개 카테고리 모두 포함
                    - recommended=true: 실제 논리
                    - recommended=false: 안내 문구 템플릿
 
                    분석이 실행된 적 없으면 404 반환
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<InventiveStepResponse>> getAnalysis(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId
    ) {
        InventiveStepResponse response = inventiveStepService.getAnalysis(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}