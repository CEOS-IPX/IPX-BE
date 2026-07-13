package ceos.ipx.domain.analysis.inventivestep.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 진보성 분석 실행 요청
 *
 * 사용자가 선행문헌함에서 D1(주인용)을 선택하고 "진보성 분석" 클릭 시 호출
 * Spring:
 *   1. D2 자동 선정 (Python)
 *   2. 이슈 카테고리 선정 (Python)
 *   3. 선정된 카테고리별 논리 생성 (Python 병렬 호출)
 *   4. DB 저장 후 결과 반환
 */
@Schema(description = "진보성 분석 실행 요청")
public record InventiveStepRequest(

        @Schema(
                description = "주인용(D1)으로 선택한 특허 출원번호",
                example = "1020170102293",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "주인용 특허 출원번호는 필수입니다.")
        String primaryApplicationNumber
) {}