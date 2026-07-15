package ceos.ipx.domain.cases.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecentCaseItemResponse {

    private final Long caseId;
    private final String title;
    private final String technicalField;
    private final String status;
    private final String statusLabel;
    private final LocalDateTime updatedAt;
}