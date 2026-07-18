package ceos.ipx.domain.cases.service.cases;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchCancelResponse;
import ceos.ipx.domain.cases.dto.response.SearchStartResponse;
import ceos.ipx.domain.cases.dto.response.SearchStatusResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.PythonSearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 선행기술 탐색 서비스 (퍼사드)
 *
 * Transactional 처리 X
 *   - Python 호출을 트랜잭션 밖에서 실행하기 위함
 *   - 트랜잭션 로직은 CaseSearchTxService에 위임
 *   - 비동기 로직은 CaseSearchAsyncService에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchService {

    private static final int DEFAULT_RESULT_COUNT = 10;

    private final CaseSearchTxService txService;
    private final CaseSearchAsyncService asyncService;
    private final SearchProgressService searchProgressService;
    private final CaseRepository caseRepository;

    /**
     * 선행기술 탐색
     */
    public SearchStartResponse startSearch(Long userId, SearchRequest request) {

        // 1. Case 및 구성요소 저장 (별도 Service의 트랜잭션 경유)
        Case caseEntity = txService.prepareCaseAndComponents(userId, request);

        // 2. 재검색 시 진행 중 상태 확인
        if (request.caseId() != null) {
            checkNotInProgress(caseEntity.getId());
        }

        // 3. resultCount 기본값 처리
        int resultCount = request.resultCount() != null
                ? request.resultCount()
                : DEFAULT_RESULT_COUNT;

        searchProgressService.markStarted(caseEntity.getId());

        // 4. 비동기 Python 호출
        asyncService.executeSearchAsync(caseEntity.getId(), request, resultCount);

        log.info("[Search] 검색 시작: caseId={}, userId={}",
                caseEntity.getId(), userId);

        return SearchStartResponse.of(caseEntity.getId());
    }

    /**
     * 검색 진행 상태 조회
     */
    public SearchStatusResponse getStatus(Long userId, Long caseId) {
        verifyOwnership(userId, caseId);
        return searchProgressService.getStatus(caseId);
    }

    /**
     * 검색 중단 요청
     */
    public SearchCancelResponse cancelSearch(Long userId, Long caseId) {
        verifyOwnership(userId, caseId);
        return searchProgressService.cancel(caseId);
    }

    private void checkNotInProgress(Long caseId) {
        try {
            SearchStatusResponse status = searchProgressService.getStatus(caseId);
            if ("in_progress".equals(status.status())) {
                throw new BusinessException(ErrorCode.SEARCH_ALREADY_IN_PROGRESS);
            }
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.SEARCH_NOT_FOUND) {
                // 이전 검색 없음 → 정상 (재검색 가능)
                return;
            }
            throw e;
        }
    }

    private void verifyOwnership(Long userId, Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));
        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }
    }
}