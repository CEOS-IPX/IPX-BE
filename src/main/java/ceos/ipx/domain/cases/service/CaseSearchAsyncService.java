package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.PythonSearchClient;
import ceos.ipx.global.python.dto.request.PythonSearchRequest;
import ceos.ipx.global.python.dto.response.PythonSearchResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 선행기술 탐색 비동기 처리 Service.
 *
 * 별도 클래스로 분리한 이유:
 *   Spring @Async는 프록시 기반이라 같은 클래스 내 self-invocation 시
 *   비동기가 안 됨. 다른 Service에서 주입받아 호출해야 프록시가 동작.
 *
 * 처리 흐름:
 *   1. Python /search 호출 (블로킹, 최대 120초)
 *   2. 응답 검증 (is_valid=false 처리)
 *   3. saveSearchResults() 별도 트랜잭션에서 결과 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchAsyncService {

    private final CaseRepository caseRepository;
    private final PriorArtRepository priorArtRepository;
    private final PythonSearchClient pythonSearchClient;
    private final PriorArtMapper priorArtMapper;

    /**
     * 백그라운드에서 Python 호출.
     * "searchTaskExecutor" 스레드풀에서 실행.
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

            saveSearchResults(caseId, response);
            log.info("[Search][Async] 검색 결과 저장 완료: caseId={}, searchId={}, count={}",
                    caseId, searchId,
                    response.results() != null ? response.results().size() : 0);

        } catch (Exception e) {
            log.error("[Search][Async] Python 호출/저장 실패: caseId={}, searchId={}",
                    caseId, searchId, e);
            // Case는 남김. 프론트가 진행률 폴링에서 Redis "failed" 상태 감지.
        }
    }

    /**
     * Python 응답을 DB에 저장 (별도 트랜잭션)
     *   - Case.keywords 갱신 (intent.keywords)
     *   - PriorArt INSERT
     *   - Case.completeSearch() 호출
     */
    @Transactional
    public void saveSearchResults(Long caseId, PythonSearchResultResponse response) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        // 1. Case.keywords 갱신 (intent.keywords 저장)
        if (response.intent() != null && response.intent().keywords() != null) {
            caseEntity.updateKeywords(response.intent().keywords());
        }

        // 2. PriorArt INSERT
        List<PythonSearchResultResponse.PatentResult> results = response.results();
        if (results != null && !results.isEmpty()) {
            for (PythonSearchResultResponse.PatentResult r : results) {
                PriorArt priorArt = priorArtMapper.toEntity(caseEntity, r);
                priorArtRepository.save(priorArt);
            }
        }

        // 3. 검색 완료 시각 갱신
        caseEntity.completeSearch();
    }
}
