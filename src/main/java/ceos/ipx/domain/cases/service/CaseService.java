package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.dto.response.CaseDetailResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.CaseStatus;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaseService {

    private final CaseRepository caseRepository;
    private final InventionComponentRepository inventionComponentRepository;
    private final PriorArtRepository priorArtRepository;

    public CaseDetailResponse getCaseDetail(Long userId, Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        CaseStatus status = determineStatus(caseEntity);

        int componentCount = Math.toIntExact(
                inventionComponentRepository.countByCaseEntity(caseEntity)
        );

        int priorArtCount = Math.toIntExact(
                priorArtRepository.countByCaseEntity(caseEntity)
        );

        return CaseDetailResponse.builder()
                .caseId(caseEntity.getId())
                .title(caseEntity.getTitle())
                .applicantName(caseEntity.getApplicantName())
                .inventorName(caseEntity.getInventorName())
                .technicalField(caseEntity.getTechnicalField())
                .description(caseEntity.getDescription())
                .userInputIpc(caseEntity.getUserInputIpc())
                .status(status.name())
                .statusLabel(status.getLabel())
                .componentCount(componentCount)
                .priorArtCount(priorArtCount)
                .reportAvailable(caseEntity.getReportCompletedAt() != null)
                .searchCompletedAt(caseEntity.getSearchCompletedAt())
                .noveltyCompletedAt(caseEntity.getNoveltyCompletedAt())
                .inventiveCompletedAt(caseEntity.getInventiveCompletedAt())
                .reportCompletedAt(caseEntity.getReportCompletedAt())
                .createdAt(caseEntity.getCreatedAt())
                .updatedAt(caseEntity.getUpdatedAt())
                .build();
    }

    private CaseStatus determineStatus(Case caseEntity) {
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