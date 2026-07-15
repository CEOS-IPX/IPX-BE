package ceos.ipx.domain.cases.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CaseUpdateResponse {

    private Long caseId;

    private String title;

    private String applicantName;

    private String inventorName;

    private LocalDateTime updatedAt;
}