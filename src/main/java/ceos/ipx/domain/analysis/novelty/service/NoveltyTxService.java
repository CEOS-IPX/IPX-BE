package ceos.ipx.domain.analysis.novelty.service;

import ceos.ipx.domain.analysis.inventivestep.dto.response.InventiveStepResponse;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.analysis.novelty.dto.response.NoveltyResponse;
import ceos.ipx.domain.analysis.novelty.entity.ComparisonResult;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyVerdict;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyComparisonRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.report.repository.ReportRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.response.novelty.PythonComponentComparison;
import ceos.ipx.global.python.dto.response.novelty.PythonNoveltyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 신규성 분석 트랜잭션 처리 Service (self-invocation 문제 해결)
 *
 * 담당:
 *   1. 분석 결과 조회
 *   2. 기존 분석 삭제 (재분석 시)
 *   3. 결과 저장 (analysis + comparisons)
 *
 * Case + Components + PriorArts 사전 조회는 CaseQueryTxService에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoveltyTxService {

    private final NoveltyAnalysisRepository analysisRepository;
    private final NoveltyComparisonRepository comparisonRepository;
    private final CaseRepository caseRepository;
    private final ReportRepository reportRepository;
    private final NoveltyMapper mapper;

    @Transactional(readOnly = true)
    public NoveltyResponse getAnalysisResponse(Case caseEntity) {
        NoveltyAnalysis analysis = analysisRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOVELTY_ANALYSIS_NOT_FOUND));

        List<NoveltyComparison> comparisonEntities = comparisonRepository.findAllByAnalysis(analysis);

        return NoveltyResponse.ofEntities(analysis, comparisonEntities, mapper);
    }

    @Transactional
    public NoveltyResponse saveAll(
            Long caseId,
            PriorArt d1,
            PythonNoveltyResponse response,
            Map<String, InventionComponent> labelToComponentMap,
            NoveltyMapper mapper
    ) {
        // 1. 기존 분석 삭제 (FK 순서: Comparison → Analysis)
        deleteExistingAnalysis(caseId);

        // 2. Case 재조회 (managed 상태 확보)
        // Service에서 넘어온 caseEntity는 이전 트랜잭션에서 조회된 detached 상태
        // dirty checking이 작동하려면 현재 트랜잭션의 영속성 컨텍스트에 있어야 함
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        // 3. NoveltyAnalysis 저장
        NoveltyAnalysis analysis = saveAnalysis(caseEntity, d1, response.overallSimilarity(), response.conclusionText());

        // 4. NoveltyComparison 저장 (구성요소별)
        List<NoveltyComparison> savedComparisons = saveComparisons(response.componentResults(), labelToComponentMap, analysis);

        // 5. Case 완료 시각 갱신 (managed 엔티티, dirty checking 작동)
        caseEntity.completeNoveltyAnalysis();

        log.info("[Novelty] 저장 완료: caseId={}, analysisId={}, comparisonCount={}",
                caseId, analysis.getId(), savedComparisons.size());

        // 6. 응답 DTO 조립
        return NoveltyResponse.ofEntities(analysis, savedComparisons, mapper);
    }

    /**
     * Case에 기존 분석이 있으면 삭제
     * 삭제 순서: NoveltyComparison → NoveltyAnalysis (FK 관계)
     */
    private void deleteExistingAnalysis(Long caseId) {
        comparisonRepository.deleteAllByCaseId(caseId);
        analysisRepository.deleteAllByCaseId(caseId);
        reportRepository.deleteAllByCaseId(caseId);

        log.info("[Novelty] 기존 분석 삭제 완료: caseId={}", caseId);
    }

    /**
     * NoveltyAnalysis + NoveltyComparison 저장 + Case 완료 시각 갱신
     *
     * response            Python 응답 (overall_similarity, conclusion_text, component_results)
     * labelToComponentMap 구성요소 라벨(A, B, ...) → InventionComponent 매핑
     */
    private NoveltyAnalysis saveAnalysis(Case caseEntity, PriorArt d1, String overallSimilarity, String conclusionText) {
        NoveltyAnalysis analysis = analysisRepository.save(
                NoveltyAnalysis.builder()
                        .caseEntity(caseEntity)
                        .d1PriorArt(d1)
                        .overallSimilarity(NoveltyVerdict.fromLabel(overallSimilarity))
                        .conclusionText(conclusionText)
                        .build()
        );

        log.info("[Novelty] Analysis 저장: id={}, d1={}, similarity={}",
                analysis.getId(), d1.getApplicationNumber(), overallSimilarity);

        return analysis;
    }

    private List<NoveltyComparison> saveComparisons(
            List<PythonComponentComparison> componentResults, Map<String, InventionComponent> labelToComponentMap, NoveltyAnalysis analysis
    ) {
        List<NoveltyComparison> savedComparisons = new ArrayList<>();

        if (componentResults != null) {
            for (PythonComponentComparison compResult : componentResults) {
                InventionComponent component = labelToComponentMap.get(compResult.componentLabel());

                if (component == null) {
                    log.warn("[Novelty] Python 응답의 라벨이 구성요소에 없음: label={}",
                            compResult.componentLabel());
                    continue;
                }

                NoveltyComparison comparison = comparisonRepository.save(
                        NoveltyComparison.builder()
                                .noveltyAnalysis(analysis)
                                .component(component)
                                .comparisonResult(ComparisonResult.fromLabel(compResult.result()))
                                .disclosureText(compResult.disclosureText())
                                .citation(compResult.citation())
                                .build()
                );

                savedComparisons.add(comparison);
            }
        }
        log.info("[Novelty] Comparisons 저장 완료: {}건", savedComparisons.size());

        return savedComparisons;
    }
}