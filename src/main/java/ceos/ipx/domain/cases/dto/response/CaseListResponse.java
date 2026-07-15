package ceos.ipx.domain.cases.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CaseListResponse {

    private final Integer totalCount;
    private final Integer pendingCount;
    private final Integer completedCount;
    private final Integer page;
    private final Integer size;
    private final Boolean hasNext;
    private final List<CaseListItemResponse> cases;
}