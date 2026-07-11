package ceos.ipx.domain.cases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 구성요소 자동 추출 요청
 */
@Schema(description = "구성요소 자동 추출 요청")
public record ComponentExtractRequest(

        @Schema(description = "발명의 명칭", example = "딥러닝 기반 자동차 진단 시스템", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "발명 명칭은 필수입니다.")
        @Size(max = 500, message = "발명 명칭은 500자 이내로 입력해주세요.")
        String title,

        @Schema(description = "발명의 핵심 기술 설명", example = "차량 센서 데이터를 딥러닝 모델로 분석하여 부품 고장을 사전 예측하는 시스템", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "발명 설명은 필수입니다.")
        String description,

        @Schema(description = "기술 분야", example = "인공지능, 자동차 진단", nullable = true)
        String technicalField
) {}