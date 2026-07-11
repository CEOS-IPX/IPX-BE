package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.entity.PriorArtSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 선행기술 조회 응답
 */
@Schema(description = "선행기술 특허 정보")
public record PriorArtResponse(

        @Schema(description = "선행기술 ID (DB PK)", example = "1")
        Long priorArtId,

        @Schema(description = "특허 출원번호", example = "1020170102293")
        String applicationNumber,

        @Schema(description = "발명의 명칭", example = "딥러닝 기반의 인공지능을 이용한 자동차 진단 방법 및 장치")
        String title,

        @Schema(description = "출원인", example = "(주)다이매틱스")
        String applicantName,

        @Schema(description = "출원일", example = "2017-08-11")
        LocalDate applicationDate,

        @Schema(description = "등록일", example = "2018-08-09", nullable = true)
        LocalDate registrationDate,

        @Schema(description = "법적 상태", example = "등록", allowableValues = {"공개", "등록", "소멸", "취하", "거절"})
        String legalStatus,

        @Schema(description = "IPC 분류 코드", example = "[\"G06F 11/00\", \"G06N 3/08\"]")
        List<String> ipcCodes,

        @Schema(description = "LLM 요약", example = "본 발명은 딥러닝 모델을 이용한 자동차 부품 고장 진단 방법을 개시한다.")
        String summary,

        @Schema(description = "기술 목적", example = "차량 부품의 고장을 사전 예측하여 유지보수 비용을 절감")
        String purpose,

        @Schema(description = "핵심 특징", example = "[\"OBD 데이터 활용\", \"CNN 기반 예측 모델\", \"실시간 진단\"]")
        List<String> features,

        @Schema(description = "매칭된 키워드", example = "[\"딥러닝\", \"자동차\", \"진단\"]")
        List<String> keywords,

        @Schema(description = "관련성 판단 근거", example = "사용자 발명과 동일하게 딥러닝 모델을 이용해 자동차 부품 고장을 예측함")
        String reason,

        @Schema(description = "RRF 스코어 (알고리즘 내부 값)", example = "0.045")
        Double rrfScore,

        @Schema(description = "관련도 등급", example = "매우 높음", allowableValues = {"매우 높음", "높음", "보통", "낮음"})
        String relevance,

        @Schema(description = "데이터 소스", example = "SEARCH", allowableValues = {"SEARCH", "MANUAL"})
        PriorArtSource source
) {

    public static PriorArtResponse of(PriorArt priorArt, String relevance) {
        return new PriorArtResponse(
                priorArt.getId(),
                priorArt.getApplicationNumber(),
                priorArt.getTitle(),
                priorArt.getApplicantName(),
                priorArt.getApplicationDate(),
                priorArt.getRegistrationDate(),
                priorArt.getLegalStatus(),
                priorArt.getIpcCodes(),
                priorArt.getSummary(),
                priorArt.getTechPurpose(),
                priorArt.getKeyFeatures(),
                priorArt.getMatchedKeywords(),
                priorArt.getReason(),
                priorArt.getRrfScore(),
                relevance,
                priorArt.getSource()
        );
    }
}