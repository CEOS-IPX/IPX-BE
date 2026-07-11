package ceos.ipx.domain.cases.dto.response;

import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.entity.PriorArtSource;

import java.time.LocalDate;
import java.util.List;

/**
 * 선행기술 조회 응답
 *
 */
public record PriorArtResponse(
        Long priorArtId,
        String applicationNumber,
        String title,
        String applicantName,
        LocalDate applicationDate,
        LocalDate registrationDate,
        String legalStatus,
        List<String> ipcCodes,
        String summary,
        String purpose,
        List<String> features,
        List<String> keywords,
        String reason,
        Double rrfScore,
        String relevance,      // "매우 높음" | "높음" | "보통" | "낮음"
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
                priorArt.getTechPurpose(),   // 엔티티는 techPurpose지만 응답은 purpose로 통일
                priorArt.getKeyFeatures(),
                priorArt.getMatchedKeywords(),
                priorArt.getReason(),
                priorArt.getRrfScore(),
                relevance,
                priorArt.getSource()
        );
    }
}