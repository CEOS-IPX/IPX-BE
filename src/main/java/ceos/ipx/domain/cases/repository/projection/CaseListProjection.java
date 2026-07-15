package ceos.ipx.domain.cases.repository.projection;

import ceos.ipx.domain.cases.entity.Case;

public interface CaseListProjection {

    Case getCaseEntity();

    Long getPriorArtCount();
}