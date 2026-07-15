package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.CaseStatus;
import org.springframework.stereotype.Component;

@Component
public class CaseStatusResolver {

    public CaseStatus resolve(Case caseEntity) {
        if (caseEntity.getReportCompletedAt() != null) {
            return CaseStatus.REPORT_COMPLETED;
        }

        if (caseEntity.getInventiveCompletedAt() != null) {
            return CaseStatus.INVENTIVE_COMPLETED;
        }

        if (caseEntity.getNoveltyCompletedAt() != null) {
            return CaseStatus.NOVELTY_COMPLETED;
        }

        if (caseEntity.getSearchCompletedAt() != null) {
            return CaseStatus.SEARCH_COMPLETED;
        }

        return CaseStatus.NOT_STARTED;
    }
}