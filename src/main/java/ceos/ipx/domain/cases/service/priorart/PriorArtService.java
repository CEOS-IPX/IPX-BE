package ceos.ipx.domain.cases.service.priorart;

import ceos.ipx.domain.cases.dto.request.AddManualRequest;
import ceos.ipx.domain.cases.dto.response.PriorArtResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.service.common.CaseQueryTxService;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.PythonSearchClient;
import ceos.ipx.global.python.dto.request.search.PythonAddManualRequest;
import ceos.ipx.global.python.dto.response.search.PythonAddManualResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import ceos.ipx.domain.cases.dto.response.PriorArtDetailResponse;
import ceos.ipx.global.opensearch.OpenSearchClient;
import ceos.ipx.global.opensearch.dto.PatentDocument;

/**
 * 선행기술 조회/관리 서비스
 *
 * 담당:
 *   - GET  /api/cases/{caseId}/prior-arts        : 선행기술 조회
 *   - POST /api/cases/{caseId}/prior-arts/manual : 수동 추가
 *
 * Python 호출을 트랜잭션 밖에서 실행하기 위해 트랜잭션 로직은
 * PriorArtTxService에 위임 (프록시 경유)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriorArtService {

    private final PriorArtTxService txService;
    private final CaseQueryTxService caseQueryTxService;
    private final PythonSearchClient pythonSearchClient;
    private final RelevanceCalculator relevanceCalculator;
    private final OpenSearchClient openSearchClient;

    /**
     * 사건의 모든 선행기술 조회 (rrf_score DESC + created_at ASC)
     * relevance는 전체 개수 기준으로 계산
     */
    public List<PriorArtResponse> getPriorArts(Long userId, Long caseId) {
        Case caseEntity = caseQueryTxService.findCaseWithAuth(userId, caseId);
        List<PriorArt> priorArts = caseQueryTxService.findPriorArts(caseEntity);
        return buildResponses(priorArts);
    }

    /**
     * 선행문헌 상세 조회
     *
     * 1. priorArtId로 PostgreSQL PriorArt 조회
     * 2. 사건 소유권 검증
     * 3. 출원번호로 OpenSearch 원본 특허 조회
     * 4. PostgreSQL 사건별 정보와 OpenSearch 원본 정보를 병합
     */
    public PriorArtDetailResponse getPriorArtDetail(Long userId, Long priorArtId) {
        PriorArt priorArt = txService.findPriorArtWithAuth(userId, priorArtId);

        PatentDocument patentDocument =
                openSearchClient.getByApplicationNumber(priorArt.getApplicationNumber());

        if (patentDocument == null) {
            throw new BusinessException(ErrorCode.PRIOR_ART_DOCUMENT_NOT_FOUND);
        }

        return PriorArtDetailResponse.of(priorArt, patentDocument);
    }

    /**
     * 사용자가 지정한 출원번호들을 prior_arts에 수동 추가
     *
     * 흐름:
     *   1. Case 조회 (권한 검증)
     *   2. 중복 필터링 (기존 prior_arts와 겹치는 것 제외)
     *   3. Python /search/add-manual 호출 (LLM 요약 등)
     *   4. 응답 받은 새 특허를 prior_arts INSERT
     *   5. 전체 목록 재조회 + relevance 계산 후 반환
     */
    public List<PriorArtResponse> addManual(Long userId, Long caseId, AddManualRequest request) {
        // 1. Case 조회 (권한 검증)
        Case caseEntity = caseQueryTxService.findCaseWithAuth(userId, caseId);

        // 2. 중복 필터링
        Set<String> duplicates = txService.findDuplicateApplicationNumbers(
                caseEntity, request.applicationNumbers());
        List<String> newApplicationNumbers = request.applicationNumbers().stream()
                .filter(num -> !duplicates.contains(num))
                .toList();

        if (!duplicates.isEmpty()) {
            log.info("[AddManual] 중복 특허 제외: caseId={}, duplicates={}", caseId, duplicates);
        }

        // 3. 모두 중복이면 예외
        if (newApplicationNumbers.isEmpty()) {
            log.warn("[AddManual] 모든 특허가 이미 존재: caseId={}, requested={}",
                    caseId, request.applicationNumbers());
            throw new BusinessException(ErrorCode.ALL_PATENTS_ALREADY_EXIST);
        }

        // 4. Python 호출 (트랜잭션 밖)
        PythonAddManualRequest pyRequest = new PythonAddManualRequest(
                newApplicationNumbers,
                new PythonAddManualRequest.SearchContext(
                        caseEntity.getTitle(),
                        caseEntity.getDescription(),
                        caseEntity.getKeywords()
                )
        );
        PythonAddManualResponse response = pythonSearchClient.addManual(pyRequest);

        // 5. 저장 (트랜잭션 프록시 경유)
        txService.saveManualPriorArts(caseId, response);

        // 6. 전체 목록 재조회 + relevance 계산
        List<PriorArt> allPriorArts = caseQueryTxService.findPriorArts(caseEntity);
        return buildResponses(allPriorArts);
    }

    /**
     * PriorArt 리스트를 응답 DTO로 변환하면서 relevance 계산.
     */
    private List<PriorArtResponse> buildResponses(List<PriorArt> priorArts) {
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