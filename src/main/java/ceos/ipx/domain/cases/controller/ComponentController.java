package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.ComponentExtractRequest;
import ceos.ipx.domain.cases.dto.response.ComponentExtractResponse;
import ceos.ipx.domain.cases.service.component.ComponentService;
import ceos.ipx.global.aop.ratelimit.RateLimit;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 구성요소 자동 추출 API
 *
 * 엔드포인트:
 *   - POST /api/searches/components/extract : AI 자동 구성요소 추출
 */
@Tag(name = "구성요소", description = "AI 자동 구성요소 추출 API")
@RestController
@RequestMapping("/api/searches/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    @Operation(
            summary = "AI 자동 구성요소 추출",
            description = """
                    발명 정보(명칭, 설명, 기술 분야)를 기반으로 청구항 구성요소를 자동 추출합니다.

                    - 검색 실행 전에 호출
                    - 응답의 label(A, B, C, ...)은 구성요소 순서대로 부여
                    """
    )
    @RateLimit(apiName = "component-extract")
    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<ComponentExtractResponse>> autoExtract(
            @RequestBody @Valid ComponentExtractRequest request
    ) {
        ComponentExtractResponse response = componentService.extract(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}