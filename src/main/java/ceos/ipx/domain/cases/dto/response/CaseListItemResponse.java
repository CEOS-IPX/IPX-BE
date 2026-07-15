package ceos.ipx.domain.cases.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CaseListItemResponse {

    private final Long caseId;
    private final String title;
    private final String applicantName;
    private final String inventorName;
    private final String technicalField;
    private final String status;
    private final String statusLabel;
    private final Integer priorArtCount;
    private final Boolean reportAvailable;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}