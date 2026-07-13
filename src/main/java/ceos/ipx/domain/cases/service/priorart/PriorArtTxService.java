package ceos.ipx.domain.cases.service.priorart;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.domain.cases.service.common.PriorArtMapper;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.response.search.PythonAddManualResponse;
import ceos.ipx.global.python.dto.response.search.PythonSearchResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 선행기술 결과 관리 트랜잭션 Service (self-invocation 문제 해결)
 *
 * 담당 트랜잭션:
 *   - 수동 추가 사전 처리 (Case 조회, 중복 필터링, context 조립)
 *   - 수동 추가 결과 저장 (PriorArt INSERT)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriorArtTxService {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final PriorArtRepository priorArtRepository;
    private final InventionComponentRepository componentRepository;
    private final PriorArtMapper priorArtMapper;

    /**
     * Case 조회 (권한 검증)
     */
    @Transactional(readOnly = true)
    public Case findCaseWithAuth(Long userId, Long caseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return caseRepository.findByIdAndUser(caseId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));
    }

    /**
     * 중복 확인: 이미 존재하는 출원번호 리스트 반환
     */
    @Transactional(readOnly = true)
    public Set<String> findDuplicateApplicationNumbers(Case caseEntity, List<String> applicationNumbers) {
        return priorArtRepository.findExistingApplicationNumbers(caseEntity, applicationNumbers);
    }

    /**
     * Python이 반환한 새 특허 정보를 prior_arts에 INSERT
     */
    @Transactional
    public void saveManualPriorArts(Long caseId, PythonAddManualResponse response) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        List<PythonSearchResultResponse.PatentResult> results = response.results();
        if (results == null || results.isEmpty()) {
            log.info("[AddManual] 저장할 새 특허 없음: caseId={}", caseId);
            return;
        }

        for (PythonSearchResultResponse.PatentResult r : results) {
            PriorArt priorArt = priorArtMapper.toEntity(caseEntity, r);
            priorArtRepository.save(priorArt);
        }
        log.info("[AddManual] {}건 저장 완료: caseId={}", results.size(), caseId);
    }

    /**
     * 사건의 전체 prior_arts 조회 (rrf_score DESC + created_at ASC)
     */
    @Transactional(readOnly = true)
    public List<PriorArt> findAllPriorArts(Case caseEntity) {
        return priorArtRepository.findByCaseEntityOrderByRrfScoreDescCreatedAtAsc(caseEntity);
    }
}