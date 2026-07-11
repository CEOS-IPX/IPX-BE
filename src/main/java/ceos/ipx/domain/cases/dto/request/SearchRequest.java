package ceos.ipx.domain.cases.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 선행기술 탐색 요청 (프론트 → Spring)
 *
 * 이 API 하나로 다음이 모두 처리됨:
 *   1. 사건(Case) 생성 or 재사용 (caseId 파라미터로 구분)
 *   2. 구성요소(InventionComponent) 저장
 *   3. 검색(prior_arts) 실행
 *
 * caseId:
 *   - null → 새 사건 생성
 *   - 값 있음 → 기존 사건 재검색 (resetAllStages + 기존 데이터 삭제)
 */
public record SearchRequest(

        Long caseId,

        @NotBlank
        @Size(max = 500)
        String title,

        @NotBlank
        String description,

        String applicantName,

        String inventorName,

        @NotBlank
        String technicalField,

        List<String> userInputIpc,

        /**
         * 탐색 전 수동 추가 특허
         */
        List<String> requiredApplicationNumbers,

        /**
         * 결과 개수 (10 ~ 30)
         */
        @Min(10)
        @Max(30)
        Integer resultCount,

        /**
         * 구성요소 배열 (최소 1개)
         * 배열 순서 = display_order
         */
        @NotEmpty
        @Valid
        List<ComponentInput> components

) {

    public record ComponentInput(
            @NotBlank
            @Size(max = 255)
            String name,

            @NotBlank
            String description
    ) {}
}