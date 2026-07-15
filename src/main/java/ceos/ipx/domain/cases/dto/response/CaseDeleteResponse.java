package ceos.ipx.domain.cases.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CaseDeleteResponse {

    private Long deletedCaseId;
}