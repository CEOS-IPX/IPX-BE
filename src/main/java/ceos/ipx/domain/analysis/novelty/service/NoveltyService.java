package ceos.ipx.domain.analysis.novelty.service;

import ceos.ipx.domain.analysis.novelty.dto.response.NoveltyResponse;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.service.common.CaseQueryTxService;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.opensearch.OpenSearchClient;
import ceos.ipx.global.opensearch.dto.PatentDocument;
import ceos.ipx.global.python.PythonNoveltyClient;
import ceos.ipx.global.python.dto.request.novelty.PythonNoveltyRequest;
import ceos.ipx.global.python.dto.request.novelty.PythonPriorArtForAnalysis;
import ceos.ipx.global.python.dto.response.novelty.PythonNoveltyResponse;
import ceos.ipx.global.python.dto.common.PythonInventionComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 신규성 분석 서비스
 *
 * 담당:
 *   - POST /api/cases/{caseId}/novelty : 실행
 *   - GET  /api/cases/{caseId}/novelty : 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoveltyService {

    /** 신규성 분석에 사용할 상위 선행기술 건수 */
    private static final int NOVELTY_PRIOR_ART_COUNT = 3;

    private final NoveltyTxService txService;
    private final CaseQueryTxService caseQueryTxService;
    private final PythonNoveltyClient pythonClient;
    private final OpenSearchClient openSearchClient;
    private final NoveltyMapper mapper;

    /**
     * 신규성 분석 실행
     * 상위 3건 선행기술을 Python에 전달하고, D1 선정 및 결과를 저장
     */
    public NoveltyResponse analyze(Long userId, Long caseId) {
        log.info("[Novelty] 시작: caseId={}", caseId);

        // 1. 사전 조회
        Case caseEntity = caseQueryTxService.findCaseWithAuth(userId, caseId);
        List<InventionComponent> components = caseQueryTxService.findComponents(caseEntity);

        List<PriorArt> priorArts = caseQueryTxService.findPriorArts(caseEntity);
        if (priorArts.isEmpty())
            throw new BusinessException(ErrorCode.PRIOR_ART_NOT_FOUND);

        // 2. 상위 3건 선정
        List<PriorArt> topPriorArts = priorArts.stream()
                .limit(NOVELTY_PRIOR_ART_COUNT)
                .toList();

        // 3. OpenSearch mget으로 청구항 조회
        List<String> appNums = topPriorArts.stream()
                .map(PriorArt::getApplicationNumber)
                .toList();

        List<PatentDocument> documents = openSearchClient.mgetByApplicationNumbers(appNums);
        Map<String, PatentDocument> docMap = documents.stream()
                .collect(Collectors.toMap(PatentDocument::applicationNumber, d -> d));

        // 4. Python 요청 조립
        List<PythonPriorArtForAnalysis> pyPriorArts = topPriorArts.stream()
                .map(pa -> mapper.toPythonPriorArt(pa, docMap.get(pa.getApplicationNumber())))
                .toList();

        List<PythonInventionComponent> pyComponents = components.stream()
                .map(c -> PythonInventionComponent.builder()
                        .label(mapper.toLabel(c.getDisplayOrder()))
                        .name(c.getName())
                        .description(c.getDescription())
                        .build())
                .toList();

        PythonNoveltyRequest pyRequest = PythonNoveltyRequest.builder()
                .inventionTitle(caseEntity.getTitle())
                .inventionDescription(caseEntity.getDescription())
                .components(pyComponents)
                .priorArts(pyPriorArts)
                .build();

        // 5. Python 호출
        PythonNoveltyResponse response = pythonClient.analyzeNovelty(pyRequest);

        // 6. D1 확정 (Python이 반환한 출원번호가 상위 3건에 있는지 검증)
        String d1AppNum = response.d1ApplicationNumber();
        PriorArt d1 = topPriorArts.stream()
                .filter(pa -> pa.getApplicationNumber().equals(d1AppNum))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[Novelty] Python이 반환한 D1이 상위 3건에 없음: {}", d1AppNum);
                    return new BusinessException(ErrorCode.INVALID_PYTHON_RESPONSE);
                });

        // 7. DB 저장 (기존 삭제 후 새로 생성)
        txService.deleteExistingAnalysis(caseId);
        Map<String, InventionComponent> labelToComponentMap = mapper.buildLabelToComponentMap(components);
        NoveltyAnalysis analysis = txService.saveResults(caseEntity, d1, response, labelToComponentMap);

        log.info("[Novelty] 완료: caseId={}, analysisId={}, similarity={}",
                caseId, analysis.getId(), response.overallSimilarity());

        // 8. 응답 조립
        return NoveltyResponse.ofPython(analysis, d1, response, labelToComponentMap);
    }

    /**
     * 저장된 신규성 분석 결과 조회
     */
    public NoveltyResponse getAnalysis(Long userId, Long caseId) {
        // 1. Case 조회 (권한 검증)
        Case caseEntity = caseQueryTxService.findCaseWithAuth(userId, caseId);

        // 2. 저장된 분석 조회
        NoveltyAnalysis analysis = txService.findAnalysis(caseEntity);

        // 3. Comparisons 조회
        List<NoveltyComparison> comparisons = txService.findComparisons(analysis);

        // 4. 응답 조립
        return NoveltyResponse.ofEntities(analysis, comparisons, mapper);
    }
}