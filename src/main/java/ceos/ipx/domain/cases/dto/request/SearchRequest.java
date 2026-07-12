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
        List<ComponentInput> components,

        @Schema(description = "추가 정보 (선택). 진보성 분석 시 활용", nullable = true)
        AdditionalInfo additionalInfo

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

        @Schema(description = "추가 정보 (선택). 진보성 분석에서만 활용, 검색에는 미사용")
        public record AdditionalInfo(

                @Schema(
                        description = "타 선행기술 대비 차별점 - 사용자가 언급한 선행기술",
                        example = "삼성전자 임베디드 보안칩 S3SSE2A - PQC를 하드웨어로 통합한 업계 최초 임베디드 보안 솔루션",
                        nullable = true
                )
                String priorArtReference,

                @Schema(
                        description = "타 선행기술 대비 차별점 - 본 발명과의 차이점",
                        example = "본 발명은 PQC 연산을 전용 하드웨어 블록이 아닌 기존 범용 MCU의 시큐어 부트로더 영역에서 처리...",
                        nullable = true
                )
                String differentiationNotes,

                @Schema(
                        description = "관련 데이터 수치 - 측정 조건 및 비교 대상",
                        example = "대상 하드웨어: ARM Cortex-M33 @ 100MHz, 알고리즘: ML-DSA-65 서명 검증, 측정 도구: DWT 사이클 카운터",
                        nullable = true
                )
                String measurementConditions,

                @Schema(
                        description = "관련 데이터 수치 - 측정 결과 및 해석",
                        example = "서명 검증 지연은 baseline 12.1ms에서 32.4ms로 168% 증가, Flash 점유는 21KB에서 78KB로 증가...",
                        nullable = true
                )
                String measurementResults
        ) {}
}