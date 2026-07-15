package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.entity.PriorArtSource;
import ceos.ipx.global.opensearch.dto.PatentDocument;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "선행문헌 상세 조회 응답")
public record PriorArtDetailResponse(

        @Schema(description = "선행문헌 ID", example = "1")
        Long priorArtId,

        @Schema(description = "소속 사건 ID", example = "1")
        Long caseId,

        @Schema(description = "출원번호", example = "1020170102293")
        String applicationNumber,

        @Schema(description = "등록번호", example = "1018923450000", nullable = true)
        String registrationNumber,

        @Schema(description = "공개번호", example = "1020190023456", nullable = true)
        String openNumber,

        @Schema(description = "발명의 명칭")
        String title,

        @Schema(description = "출원인명", nullable = true)
        String applicantName,

        @Schema(description = "발명자명", nullable = true)
        String inventorName,

        @Schema(description = "출원일", example = "2017-08-11", nullable = true)
        LocalDate applicationDate,

        @Schema(description = "공개일", example = "2019-02-20", nullable = true)
        LocalDate openDate,

        @Schema(description = "등록일", example = "2020-03-12", nullable = true)
        LocalDate registrationDate,

        @Schema(description = "법적 상태", example = "등록", nullable = true)
        String legalStatus,

        @Schema(description = "IPC 코드 목록")
        List<String> ipcCodes,

        @Schema(description = "CPC 코드 목록")
        List<String> cpcCodes,

        @Schema(description = "특허 초록", nullable = true)
        String abstractText,

        @Schema(description = "독립 청구항 목록")
        List<String> independentClaims,

        @Schema(description = "선행문헌 추가 출처")
        PriorArtSource source,

        @Schema(description = "RRF 점수", example = "0.045")
        Double rrfScore,

        @Schema(description = "추천 이유", nullable = true)
        String reason,

        @Schema(description = "핵심 요약", nullable = true)
        String summary,

        @Schema(description = "기술 목적", nullable = true)
        String techPurpose,

        @Schema(description = "주요 특징")
        List<String> keyFeatures,

        @Schema(description = "관련 키워드")
        List<String> matchedKeywords,

        @Schema(description = "선행문헌 추가 일시")
        LocalDateTime createdAt

) {

    public static PriorArtDetailResponse of(
            PriorArt priorArt,
            PatentDocument patentDocument
    ) {
        return new PriorArtDetailResponse(
                priorArt.getId(),
                priorArt.getCaseEntity().getId(),
                priorArt.getApplicationNumber(),

                patentDocument.registrationNumber(),
                patentDocument.openNumber(),

                preferPostgres(priorArt.getTitle(), patentDocument.title()),
                preferPostgres(
                        priorArt.getApplicantName(),
                        patentDocument.applicantName()
                ),
                patentDocument.inventorName(),

                preferPostgres(
                        priorArt.getApplicationDate(),
                        patentDocument.applicationDate()
                ),
                patentDocument.openDate(),
                preferPostgres(
                        priorArt.getRegistrationDate(),
                        patentDocument.registrationDate()
                ),
                preferPostgres(
                        priorArt.getLegalStatus(),
                        patentDocument.legalStatus()
                ),

                preferPostgresList(
                        priorArt.getIpcCodes(),
                        patentDocument.ipcCodes()
                ),
                emptyIfNull(patentDocument.cpcCodes()),
                patentDocument.abstractClean(),
                emptyIfNull(patentDocument.claimsIndependent()),

                priorArt.getSource(),
                priorArt.getRrfScore(),
                priorArt.getReason(),
                priorArt.getSummary(),
                priorArt.getTechPurpose(),
                emptyIfNull(priorArt.getKeyFeatures()),
                emptyIfNull(priorArt.getMatchedKeywords()),
                priorArt.getCreatedAt()
        );
    }

    private static <T> T preferPostgres(T postgresValue, T openSearchValue) {
        return postgresValue != null ? postgresValue : openSearchValue;
    }

    private static <T> List<T> preferPostgresList(
            List<T> postgresValue,
            List<T> openSearchValue
    ) {
        if (postgresValue != null && !postgresValue.isEmpty()) {
            return List.copyOf(postgresValue);
        }

        return emptyIfNull(openSearchValue);
    }

    private static <T> List<T> emptyIfNull(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}