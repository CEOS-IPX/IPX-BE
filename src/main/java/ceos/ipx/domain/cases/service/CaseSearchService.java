package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchStartResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.global.python.PythonSearchClient;
import ceos.ipx.global.python.dto.response.PythonCancelResponse;
import ceos.ipx.global.python.dto.response.PythonSearchStatusResponse;
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
    private final PythonSearchClient pythonSearchClient;

    /**
     * 선행기술 탐색
     */
    public SearchStartResponse startSearch(Long userId, SearchRequest request) {
        // 1. Case 및 구성요소 저장 (별도 Service의 트랜잭션 경유)
        Case caseEntity = txService.prepareCaseAndComponents(userId, request);

        // 2. search_id 발급
        String searchId = UUID.randomUUID().toString();

        // 3. resultCount 기본값 처리
        int resultCount = request.resultCount() != null
                ? request.resultCount()
                : DEFAULT_RESULT_COUNT;

        // 4. 비동기 Python 호출
        asyncService.executeSearchAsync(caseEntity.getId(), searchId, request, resultCount);

        log.info("[Search] 검색 시작: caseId={}, searchId={}, userId={}",
                caseEntity.getId(), searchId, userId);

        return SearchStartResponse.of(searchId, caseEntity.getId());
    }

    /**
     * 검색 진행 상태 조회
     * Python /search/{searchId}/status 그대로 프록시
     */
    public PythonSearchStatusResponse getStatus(String searchId) {
        return pythonSearchClient.getStatus(searchId);
    }

    /**
     * 검색 중단 요청
     * Python /search/{searchId}/cancel 그대로 프록시
     */
    public PythonCancelResponse cancelSearch(String searchId) {
        return pythonSearchClient.cancel(searchId);
    }
}