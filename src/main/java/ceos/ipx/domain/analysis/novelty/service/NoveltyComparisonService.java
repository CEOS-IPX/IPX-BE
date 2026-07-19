package ceos.ipx.domain.analysis.novelty.service;

import ceos.ipx.domain.analysis.novelty.dto.request.NoveltyComparisonUpdateRequest;
import ceos.ipx.domain.analysis.novelty.dto.response.NoveltyComparisonUpdateResponse;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyComparisonRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoveltyComparisonService {

    private final NoveltyComparisonRepository noveltyComparisonRepository;

    @Transactional
    public NoveltyComparisonUpdateResponse update(
            Long userId,
            Long comparisonId,
            NoveltyComparisonUpdateRequest request
    ) {
        NoveltyComparison comparison = noveltyComparisonRepository.findById(comparisonId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOVELTY_COMPARISON_NOT_FOUND)
                );

        Case caseEntity = comparison.getAnalysis().getCaseEntity();

        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        comparison.updateComparison(
                request.comparisonResult(),
                request.citation()
        );

        return NoveltyComparisonUpdateResponse.from(comparison);
    }
}