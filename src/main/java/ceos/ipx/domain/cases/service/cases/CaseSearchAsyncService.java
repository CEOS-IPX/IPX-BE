package ceos.ipx.domain.cases.service.cases;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.global.python.PythonSearchClient;
import ceos.ipx.global.python.dto.request.search.PythonSearchRequest;
import ceos.ipx.global.python.dto.response.search.PythonSearchResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 선행기술 탐색 비동기 처리 Service (self-invocation 문제 해결)
 *
 * 처리 흐름:
 *   1. Python /search 호출 (블로킹, 최대 120초)
 *   2. 응답 검증 (is_valid=false 처리)
 *   3. CaseSearchTxService.saveSearchResults() 호출 (프록시 경유 트랜잭션)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchAsyncService {

    private final PythonSearchClient pythonSearchClient;
    private final CaseSearchTxService txService;

    /**
     * 백그라운드에서 Python 호출
     * "searchTaskExecutor" 스레드풀에서 실행
     */
    @Async("searchTaskExecutor")
    public void executeSearchAsync(Long caseId, String searchId, SearchRequest request, int resultCount) {
        PythonSearchRequest pyRequest = PythonSearchRequest.builder()
                .searchId(searchId)
                .title(request.title())
                .description(request.description())
                .technicalField(request.technicalField())
                .userInputIpc(request.userInputIpc())
                .resultCount(resultCount)
                .requiredApplicationNumbers(request.requiredApplicationNumbers())
                .build();

        try {
            PythonSearchResultResponse response = pythonSearchClient.executeSearch(pyRequest);

            if (response == null) {
                log.error("[Search][Async] Python 응답 null: searchId={}", searchId);
                return;
            }

            if (Boolean.FALSE.equals(response.isValid())) {
                log.warn("[Search][Async] Python 검증 실패: searchId={}, reason={}",
                        searchId, response.reasonInvalid());
                return;
            }

            // 별도 Service의 트랜잭션 경유로 저장
            txService.saveSearchResults(caseId, response);

            log.info("[Search][Async] 검색 결과 저장 완료: caseId={}, searchId={}, count={}",
                    caseId, searchId,
                    response.results() != null ? response.results().size() : 0);

        } catch (Exception e) {
            log.error("[Search][Async] Python 호출/저장 실패: caseId={}, searchId={}",
                    caseId, searchId, e);
        }
    }
}
