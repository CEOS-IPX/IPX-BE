package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.dto.request.CaseSortType;
import ceos.ipx.domain.cases.dto.request.CaseStatusGroup;
import ceos.ipx.domain.cases.dto.response.CaseDetailResponse;
import ceos.ipx.domain.cases.dto.response.CaseListItemResponse;
import ceos.ipx.domain.cases.dto.response.CaseListResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.CaseStatus;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.domain.cases.repository.projection.CaseListProjection;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaseService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CaseRepository caseRepository;
    private final InventionComponentRepository inventionComponentRepository;
    private final PriorArtRepository priorArtRepository;
    private final CaseStatusResolver caseStatusResolver;

    public CaseDetailResponse getCaseDetail(Long userId, Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        CaseStatus status = caseStatusResolver.resolve(caseEntity);

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

    public CaseListResponse getCaseList(
            Long userId,
            CaseStatusGroup statusGroup,
            String keyword,
            CaseSortType sortType,
            int page,
            int size
    ) {
        validatePaging(page, size);

        String normalizedKeyword = normalizeKeyword(keyword);
        Boolean completed = convertCompletedFilter(statusGroup);
        Pageable pageable = PageRequest.of(page, size, createSort(sortType));

        Page<CaseListProjection> casePage = caseRepository.findCaseList(
                userId,
                completed,
                normalizedKeyword,
                pageable
        );

        List<CaseListItemResponse> cases = casePage.getContent().stream()
                .map(this::toCaseListItemResponse)
                .toList();

        return CaseListResponse.builder()
                .totalCount(Math.toIntExact(caseRepository.countByUserId(userId)))
                .pendingCount(Math.toIntExact(
                        caseRepository.countByUserIdAndReportCompletedAtIsNull(userId)
                ))
                .completedCount(Math.toIntExact(
                        caseRepository.countByUserIdAndReportCompletedAtIsNotNull(userId)
                ))
                .page(page)
                .size(size)
                .hasNext(casePage.hasNext())
                .cases(cases)
                .build();
    }

    private CaseListItemResponse toCaseListItemResponse(
            CaseListProjection projection
    ) {
        Case caseEntity = projection.getCaseEntity();
        CaseStatus status = caseStatusResolver.resolve(caseEntity);

        return CaseListItemResponse.builder()
                .caseId(caseEntity.getId())
                .title(caseEntity.getTitle())
                .applicantName(caseEntity.getApplicantName())
                .inventorName(caseEntity.getInventorName())
                .technicalField(caseEntity.getTechnicalField())
                .status(status.name())
                .statusLabel(status.getLabel())
                .priorArtCount(Math.toIntExact(projection.getPriorArtCount()))
                .reportAvailable(caseEntity.getReportCompletedAt() != null)
                .createdAt(caseEntity.getCreatedAt())
                .updatedAt(caseEntity.getUpdatedAt())
                .build();
    }

    private void validatePaging(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }

        return keyword.trim();
    }

    private Boolean convertCompletedFilter(CaseStatusGroup statusGroup) {
        return switch (statusGroup) {
            case ALL -> null;
            case PENDING -> false;
            case COMPLETED -> true;
        };
    }

    private Sort createSort(CaseSortType sortType) {
        return switch (sortType) {
            case LATEST -> Sort.by(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            );
            case OLDEST -> Sort.by(
                    Sort.Order.asc("createdAt"),
                    Sort.Order.asc("id")
            );
            case TITLE -> Sort.by(
                    Sort.Order.asc("title").ignoreCase(),
                    Sort.Order.desc("id")
            );
        };
    }
}