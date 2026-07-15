package ceos.ipx.domain.cases.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecentCaseListResponse {

    private final List<RecentCaseItemResponse> cases;
}