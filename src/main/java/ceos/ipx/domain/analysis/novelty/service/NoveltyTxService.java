package ceos.ipx.domain.analysis.novelty.service;

import ceos.ipx.domain.analysis.novelty.entity.ComparisonResult;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyVerdict;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyComparisonRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.response.novelty.PythonComponentComparison;
import ceos.ipx.global.python.dto.response.novelty.PythonNoveltyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public NoveltyAnalysis findAnalysis(Case caseEntity) {
        return analysisRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOVELTY_ANALYSIS_NOT_FOUND));
    }
    @Transactional(readOnly = true)
    public List<NoveltyComparison> findComparisons(NoveltyAnalysis analysis) {
        return comparisonRepository.findAllByAnalysis(analysis);
    }

    /**
     * Case에 기존 분석이 있으면 삭제
     * 삭제 순서: NoveltyComparison → NoveltyAnalysis (FK 관계)
     */
    @Transactional
    public void deleteExistingAnalysis(Long caseId) {
        comparisonRepository.deleteAllByCaseId(caseId);
        analysisRepository.deleteAllByCaseId(caseId);
        log.info("[Novelty] 기존 분석 삭제 완료: caseId={}", caseId);
    }

    /**
     * NoveltyAnalysis + NoveltyComparison 저장 + Case 완료 시각 갱신
     *
     * response            Python 응답 (overall_similarity, conclusion_text, component_results)
     * labelToComponentMap 구성요소 라벨(A, B, ...) → InventionComponent 매핑
     */
    @Transactional
    public NoveltyAnalysis saveResults(
            Case caseEntity,
            PriorArt d1,
            PythonNoveltyResponse response,
            Map<String, InventionComponent> labelToComponentMap
    ) {
        // 1. NoveltyAnalysis 저장
        NoveltyAnalysis analysis = NoveltyAnalysis.builder()
                .caseEntity(caseEntity)
                .d1PriorArt(d1)
                .overallSimilarity(NoveltyVerdict.fromLabel(response.overallSimilarity()))
                .conclusionText(response.conclusionText())
                .build();
        analysis = analysisRepository.save(analysis);
        log.info("[Novelty] Analysis 저장: id={}, d1={}, similarity={}",
                analysis.getId(), d1.getApplicationNumber(), response.overallSimilarity());

        // 2. NoveltyComparison 저장 (구성요소별)
        if (response.componentResults() != null) {
            for (PythonComponentComparison compResult : response.componentResults()) {
                InventionComponent component = labelToComponentMap.get(compResult.componentLabel());
                if (component == null) {
                    log.warn("[Novelty] Python 응답의 라벨이 구성요소에 없음: label={}",
                            compResult.componentLabel());
                    continue;
                }

                NoveltyComparison comparison = NoveltyComparison.builder()
                        .noveltyAnalysis(analysis)
                        .component(component)
                        .comparisonResult(ComparisonResult.fromLabel(compResult.result()))
                        .disclosureText(compResult.disclosureText())
                        .citation(compResult.citation())
                        .build();
                comparisonRepository.save(comparison);
            }
        }
        log.info("[Novelty] Comparisons 저장 완료");

        // 3. Case 완료 시각 갱신
        caseEntity.completeNoveltyAnalysis();

        return analysis;
    }
}