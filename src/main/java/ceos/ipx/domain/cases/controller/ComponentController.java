package ceos.ipx.domain.cases.controller;

import ceos.ipx.domain.cases.dto.request.ComponentExtractRequest;
import ceos.ipx.domain.cases.dto.response.ComponentExtractResponse;
import ceos.ipx.domain.cases.service.ComponentService;
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
 * 구성요소 자동 추출 API
 *
 * 엔드포인트:
 *   - POST /api/components/auto-extract : AI 자동 구성요소 추출
 */
@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    /**
     * AI 자동 구성요소 추출
     */
    @PostMapping("/auto-extract")
    public ResponseEntity<ApiResponse<ComponentExtractResponse>> autoExtract(
            @RequestBody @Valid ComponentExtractRequest request
    ) {
        ComponentExtractResponse response = componentService.extract(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}