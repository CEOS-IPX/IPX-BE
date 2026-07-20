package ceos.ipx.domain.analysis.novelty.controller;

import ceos.ipx.domain.analysis.novelty.dto.request.NoveltyComparisonUpdateRequest;
import ceos.ipx.domain.analysis.novelty.dto.response.NoveltyComparisonUpdateResponse;
import ceos.ipx.domain.analysis.novelty.service.NoveltyComparisonService;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "신규성 비교 결과",
        description = "신규성 분석의 구성요소별 비교 결과 수정 API"
)
@RestController
@RequestMapping("/api/novelty-comparisons")
@RequiredArgsConstructor
public class NoveltyComparisonController {

    private final NoveltyComparisonService noveltyComparisonService;

    @Operation(
            summary = "신규성 비교 결과 수정",
            description = """
                    특정 구성요소의 신규성 비교 결과와 선행문헌 인용 부분을 수정합니다.

                    처리 기준:
                    - comparisonId로 신규성 비교 결과 조회
                    - 해당 비교 결과가 속한 사건의 소유권 검증
                    - comparisonResult와 citation만 수정
                    - citation은 null로 변경 가능
                    - 신규성 분석 전체를 다시 실행하지 않음
                    - Python AI 서버와 OpenSearch를 호출하지 않음
                    - 사건의 신규성 분석 완료 시각을 변경하지 않음
                    """
    )
    @PatchMapping("/{comparisonId}")
    public ResponseEntity<ApiResponse<NoveltyComparisonUpdateResponse>> update(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId,

            @Parameter(
                    description = "신규성 비교 결과 ID",
                    example = "1"
            )
            @PathVariable Long comparisonId,

            @RequestBody
            @Valid
            NoveltyComparisonUpdateRequest request
    ) {
        NoveltyComparisonUpdateResponse response =
                noveltyComparisonService.update(
                        userId,
                        comparisonId,
                        request
                );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}