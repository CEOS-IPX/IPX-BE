package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchStartResponse;
import ceos.ipx.domain.cases.entity.Case;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 선행기술 탐색 서비스 (파서드 서비스)
 *
 * 흐름:
 *   1. TxService에 위임 (동기 트랜잭션)
 *   2. AsyncService에 위임 (비동기 Python 호출)
 *   3. 프론트에 즉시 응답
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchService {

    private static final int DEFAULT_RESULT_COUNT = 10;

    private final CaseSearchTxService txService;
    private final CaseSearchAsyncService asyncService;

    /**
     * 선행기술 탐색
     */
    public SearchStartResponse startSearch(Long userId, SearchRequest request) {
        // 1. Case 및 구성요소 저장 (self-invocation 해결)
        Case caseEntity = txService.prepareCaseAndComponents(userId, request);

        // 2. search_id 발급
        String searchId = UUID.randomUUID().toString();

        // 3. resultCount 기본값 처리
        int resultCount = request.resultCount() != null
                ? request.resultCount()
                : DEFAULT_RESULT_COUNT;

        // 4. 비동기 Python 호출 트리거 (별도 Service의 @Async 경유)
        asyncService.executeSearchAsync(caseEntity.getId(), searchId, request, resultCount);

        log.info("[Search] 검색 시작: caseId={}, searchId={}, userId={}",
                caseEntity.getId(), searchId, userId);

        return SearchStartResponse.of(searchId, caseEntity.getId());
    }
}