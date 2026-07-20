package ceos.ipx.domain.cases.service.cases;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.global.exception.BusinessException;
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
 *   1. Python /search 호출
 *   2. 응답 검증
 *   3. CaseSearchTxService.saveSearchResults() 호출 (프록시 경유 트랜잭션)
 *
 * 응답 케이스:
 *   응답 null: Python 서버 응답 없음 (Spring에서 markFailed)
 *   is_valid=false: 사용자 입력 부적절 또는 취소 (Python이 이미 Redis 상태 저장)
 *   results 비어있음: 검색 결과 0건 (Python이 이미 Redis 상태 저장)
 *   정상: DB 저장 → afterCommit 에서 Redis 완료 상태 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchAsyncService {

    private final PythonSearchClient pythonSearchClient;
    private final CaseSearchTxService txService;
    private final SearchProgressService searchProgressService;

    /**
     * 백그라운드에서 Python 호출
     * "searchTaskExecutor" 스레드풀에서 실행
     */
    @Async("searchTaskExecutor")
    public void executeSearchAsync(Long caseId, SearchRequest request, int resultCount) {
        PythonSearchRequest pyRequest = PythonSearchRequest.builder()
                .caseId(String.valueOf(caseId))
                .title(request.title())
                .description(request.description())
                .technicalField(request.technicalField())
                .userInputIpc(request.userInputIpc())
                .resultCount(resultCount)
                .requiredApplicationNumbers(request.requiredApplicationNumbers())
                .build();

        try {
            PythonSearchResultResponse response = pythonSearchClient.executeSearch(pyRequest);

            // 케이스 1: is_valid=false (사용자 입력 부적절 또는 취소)
            // Python이 이미 mark_invalid_input 또는 취소 시 별도 처리로 Redis 상태 저장함
            // Spring은 DB 저장 없이 종료
            if (Boolean.FALSE.equals(response.isValid())) {
                log.warn("[Search][Async] 검색 완료 (invalid): caseId={}, reason={}",
                        caseId, response.reasonInvalid());
                return;
            }

            // 케이스 2: results 비어있음 (검색 결과 0건)
            // Python이 이미 mark_no_results로 Redis 상태 저장함
            // Spring은 DB 저장 없이 종료
            if (response.results() == null || response.results().isEmpty()) {
                log.info("[Search][Async] 검색 결과 0건: caseId={}", caseId);
                return;
            }

            // 케이스 3: 정상 완료 + 결과 있음
            // TxService의 afterCommit 훅에서 markCompleted 호출
            txService.saveSearchResults(caseId, response);

            log.info("[Search][Async] 검색 결과 저장 완료: caseId={}, count={}",
                    caseId, response.results().size());

        } catch (BusinessException e) {
            log.error("[Search][Async] 비즈니스 예외 발생: caseId={}, errorCode={}", caseId, e.getErrorCode(), e);

            searchProgressService.markFailed(caseId, e.getErrorCode().getMessage());

        } catch (Exception e) {
            log.error("[Search][Async] 예상치 못한 시스템 예외 발생 (DB 또는 기타): caseId={}", caseId, e);

            // 지저분한 영어 에러 대신, 안내 문구 저장
            searchProgressService.markFailed(caseId, "검색 결과를 처리하는 중 서버 오류가 발생했습니다.");
        }
    }
}
