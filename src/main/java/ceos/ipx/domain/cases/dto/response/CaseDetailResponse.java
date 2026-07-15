package ceos.ipx.domain.cases.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CaseDetailResponse {

    private Long caseId;
    private String title;
    private String applicantName;
    private String inventorName;
    private String technicalField;
    private String description;
    private List<String> userInputIpc;

    private String status;
    private String statusLabel;

    private Integer componentCount;
    private Integer priorArtCount;
    private Boolean reportAvailable;

    private LocalDateTime searchCompletedAt;
    private LocalDateTime noveltyCompletedAt;
    private LocalDateTime inventiveCompletedAt;
    private LocalDateTime reportCompletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}