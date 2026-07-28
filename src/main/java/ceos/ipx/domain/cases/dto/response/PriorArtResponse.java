package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.domain.cases.entity.PriorArt;
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

        @Schema(
                description = "발명의 명칭",
                example = "딥러닝 기반의 인공지능을 이용한 자동차 진단 방법 및 장치"
        )
        String title,

        @Schema(description = "출원인", example = "(주)다이매틱스")
        String applicantName,

        @Schema(description = "출원일", example = "2017-08-11")
        LocalDate applicationDate,

        @Schema(
                description = "법적 상태",
                example = "등록",
                allowableValues = {
                        "공개",
                        "등록",
                        "소멸",
                        "취하",
                        "거절"
                }
        )
        String legalStatus,

        @Schema(
                description = "매칭된 키워드",
                example = "[\"딥러닝\", \"자동차\", \"진단\"]"
        )
        List<String> keywords,

        @Schema(
                description = "관련성 판단 근거",
                example = "사용자 발명과 동일하게 딥러닝 모델을 이용해 자동차 부품 고장을 예측함"
        )
        String reason,

        @Schema(
                description = "RRF 스코어 (알고리즘 내부 값)",
                example = "0.045"
        )
        Double rrfScore,

        @Schema(
                description = "관련도 등급",
                example = "VERY_HIGH",
                allowableValues = {
                        "VERY_HIGH",
                        "HIGH",
                        "MEDIUM",
                        "LOW",
                        "VERY_LOW"
                }
        )
        Relevance relevance

) {

    public static PriorArtResponse of(
            PriorArt priorArt,
            Relevance relevance
    ) {
        return new PriorArtResponse(
                priorArt.getId(),
                priorArt.getApplicationNumber(),
                priorArt.getTitle(),
                priorArt.getApplicantName(),
                priorArt.getApplicationDate(),
                priorArt.getLegalStatus(),
                priorArt.getMatchedKeywords(),
                priorArt.getReason(),
                priorArt.getRrfScore(),
                relevance
        );
    }
}