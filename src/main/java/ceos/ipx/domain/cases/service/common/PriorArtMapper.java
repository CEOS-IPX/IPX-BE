package ceos.ipx.domain.cases.service.common;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.entity.PriorArtSource;
import ceos.ipx.global.python.dto.response.search.PythonSearchResultResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * Python PatentResult → PriorArt 엔티티 변환
 */
@Component
public class PriorArtMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Python PatentResult로부터 PriorArt 엔티티 생성
     */
    public PriorArt toEntity(Case caseEntity, PythonSearchResultResponse.PatentResult r) {
        return PriorArt.builder()
                .caseEntity(caseEntity)
                .applicationNumber(r.applicationNumber())
                .title(r.title())
                .applicantName(r.applicantName())
                .applicationDate(parseDate(r.applicationDate()))
                .registrationDate(parseDate(r.registrationDate()))
                .legalStatus(r.legalStatus())
                .ipcCodes(nullSafeList(r.ipcCodes()))
                .source(determineSource(r.sources()))
                .rrfScore(r.rrfScore())
                .relevanceScore(r.relevanceScore() != null ? r.relevanceScore() : 0)
                .summary(r.summary())
                .techPurpose(r.purpose())
                .keyFeatures(nullSafeList(r.features()))
                .matchedKeywords(nullSafeList(r.keywords()))
                .reason(r.reason())
                .build();
    }

    /**
     * sources 리스트로부터 PriorArtSource 결정
     * "manual"이 포함되어 있으면 MANUAL, 아니면 SEARCH
     */
    private PriorArtSource determineSource(List<String> sources) {
        if (sources != null && sources.contains("manual")) {
            return PriorArtSource.MANUAL;
        }
        return PriorArtSource.SEARCH;
    }

    /**
     * "2017-08-11" 형식의 문자열 → LocalDate.
     * null이거나 파싱 실패면 null 반환.
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private <T> List<T> nullSafeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}
