package ceos.ipx.domain.cases.service.common;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Case 기반 분석(신규성/진보성/선행기술)에서 공통으로 쓰이는 조회 트랜잭션 Service
 *
 * InventiveStepTxService, NoveltyTxService, PriorArtTxService에 중복 구현되어 있던
 * Case 조회 + 권한 검증, 구성요소 조회, 선행기술 조회 로직을 통합
 */
@Service
@RequiredArgsConstructor
public class CaseQueryTxService {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final InventionComponentRepository componentRepository;
    private final PriorArtRepository priorArtRepository;

    /**
     * Case 조회 (권한 검증)
     */
    @Transactional(readOnly = true)
    public Case findCaseWithAuth(Long userId, Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        return caseEntity;
    }

    /**
     * Case의 구성요소 조회 (displayOrder ASC)
     */
    @Transactional(readOnly = true)
    public List<InventionComponent> findComponents(Case caseEntity) {
        List<InventionComponent> components = componentRepository.findByCaseEntityOrderByDisplayOrderAsc(caseEntity);

        if (components.isEmpty())
            throw new BusinessException(ErrorCode.COMPONENTS_REQUIRED);

        return components;
    }

    /**
     * Case의 모든 선행기술 조회 (rrf_score DESC + created_at ASC)
     */
    @Transactional(readOnly = true)
    public List<PriorArt> findPriorArts(Case caseEntity) {
        return priorArtRepository.findByCaseEntityOrderByRrfScoreDescCreatedAtAsc(caseEntity);
    }
}
