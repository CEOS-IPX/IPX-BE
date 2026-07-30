package ceos.ipx.domain.cases.dto.analysis;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;

import java.util.List;

public record CaseAnalysisContext(
        Case caseEntity,
        List<InventionComponent> components,
        List<PriorArt> priorArts
) {}
