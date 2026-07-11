package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.dto.response.PriorArtResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 선행기술 조회
 *
 * 담당:
 *   - GET  /api/cases/{caseId}/prior-arts        : 선행기술 조회
 *   - POST /api/cases/{caseId}/prior-arts/manual : 수동 추가 (Step 3 후반)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriorArtService {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final PriorArtRepository priorArtRepository;
    private final RelevanceCalculator relevanceCalculator;

    /**
     * 사건의 모든 선행기술 조회 (관련도 순)
     */
    @Transactional(readOnly = true)
    public List<PriorArtResponse> getPriorArts(Long userId, Long caseId) {
        // 1. User + Case 조회 (권한 검증)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Case caseEntity = caseRepository.findByIdAndUser(caseId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        // 2. PriorArt 전체 조회
        List<PriorArt> priorArts = priorArtRepository.findByCaseEntityOrderByRrfScoreDescCreatedAtAsc(caseEntity);

        // 3. relevance 계산 후 응답
        int total = priorArts.size();
        return priorArts.stream()
                .map(pa -> {
                    int rank = priorArts.indexOf(pa) + 1;
                    String relevance = relevanceCalculator.toRelevance(rank, total);
                    return PriorArtResponse.of(pa, relevance);
                })
                .toList();
    }
}