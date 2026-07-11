package ceos.ipx.domain.cases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 선행기술 탐색 요청
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
@Schema(description = "선행기술 탐색 실행 요청")
public record SearchRequest(

        @Schema(description = "사건 ID (재검색 시 지정). null이면 새 사건 생성", example = "null", nullable = true)
        Long caseId,

        @Schema(description = "발명의 명칭", example = "딥러닝 기반 자동차 진단 시스템", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "발명 명칭은 필수입니다.")
        @Size(max = 500, message = "발명 명칭은 500자 이내로 입력해주세요.")
        String title,

        @Schema(description = "발명의 핵심 기술 설명", example = "차량 센서 데이터를 딥러닝 모델로 분석하여 부품 고장을 사전 예측하는 시스템", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "발명 설명은 필수입니다.")
        String description,

        @Schema(description = "출원인 명칭", example = "홍길동", nullable = true)
        String applicantName,

        @Schema(description = "발명자 명칭", example = "홍길동", nullable = true)
        String inventorName,

        @Schema(description = "기술 분야", example = "인공지능, 자동차 진단", nullable = true)
        String technicalField,

        @Schema(description = "변리사가 신뢰하는 IPC 코드 (없으면 Python이 추정)", example = "[\"G06N 3/08\"]", nullable = true)
        List<String> userInputIpc,

        @Schema(description = "탐색 전 반드시 결과에 포함할 출원번호 리스트", example = "[\"1020200012345\"]", nullable = true)
        List<String> requiredApplicationNumbers,

        @Schema(description = "결과 개수 (10~30). null이면 기본값 10 적용", example = "10", minimum = "10", maximum = "30", nullable = true)
        @Min(value = 10, message = "결과 개수는 최소 10개여야 합니다.")
        @Max(value = 30, message = "결과 개수는 최대 30개까지 가능합니다.")
        Integer resultCount,

        @Schema(description = "구성요소 배열 (최소 1개). 배열 순서가 곧 표시 순서(A, B, C...)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "구성요소는 최소 1개 이상 필요합니다.")
        @Valid
        List<ComponentInput> components

) {

        @Schema(description = "구성요소 개별 항목")
        public record ComponentInput(

                @Schema(description = "구성요소 명칭", example = "센서 데이터 수집부", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "구성요소 명칭은 필수입니다.")
                @Size(max = 255, message = "구성요소 명칭은 255자 이내로 입력해주세요.")
                String name,

                @Schema(description = "구성요소 설명", example = "차량 센서로부터 실시간 데이터를 수집하는 모듈", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "구성요소 설명은 필수입니다.")
                String description
        ) {}
}