package ceos.ipx.domain.analysis.novelty.controller;

import ceos.ipx.domain.analysis.novelty.dto.response.NoveltyResponse;
import ceos.ipx.domain.analysis.novelty.service.NoveltyService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 신규성 분석 API
 *
 * 엔드포인트:
 *   - POST /api/cases/{caseId}/novelty : 신규성 분석 실행
 *   - GET  /api/cases/{caseId}/novelty : 저장된 분석 결과 조회
 */
@Tag(name = "신규성 분석", description = "신규성 분석 실행 및 조회 API")
@RestController
@RequestMapping("/api/cases/{caseId}/novelty")
@RequiredArgsConstructor
public class NoveltyController {

    private final NoveltyService noveltyService;

    @Operation(
            summary = "신규성 분석 실행",
            description = """
                    사건의 상위 3건 선행기술을 대상으로 신규성 분석을 실행합니다.

                    처리 흐름:
                    1. 상위 3건 선행기술의 청구항을 OpenSearch에서 조회
                    2. Python 서버가 3건에 대해 병렬 LLM 분석
                    3. 가장 유사한 1건을 D1(주인용)으로 선정
                    4. DB 저장 (기존 분석이 있으면 삭제 후 새로 생성)

                    결과에 포함되는 정보:
                    - D1 특허 정보
                    - 전체 유사도 판단 (VERY_HIGH / HIGH / MEDIUM / LOW)
                    - 신규성 판단 결론
                    - 구성요소별 대비 결과 (동일 / 유사 / 신규)
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<NoveltyResponse>> analyze(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId
    ) {
        NoveltyResponse response = noveltyService.analyze(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(
            summary = "신규성 분석 결과 조회",
            description = """
                    이전에 실행된 신규성 분석 결과를 조회합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<NoveltyResponse>> getAnalysis(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "사건 ID", example = "1")
            @PathVariable Long caseId
    ) {
        NoveltyResponse response = noveltyService.getAnalysis(userId, caseId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
