package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveArgumentRepository;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 진보성 분석 트랜잭션 처리 Service (self-invoction 문제 해결)
 *
 * 담당:
 *   1. 기존 분석 삭제 (재분석 시)
 *   2. 결과 저장 (analysis + arguments)
 *
 * Case + Components + PriorArts 사전 조회는 CaseQueryTxService에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventiveStepTxService {

    private final CaseRepository caseRepository;
    private final InventiveStepAnalysisRepository analysisRepository;
    private final InventiveArgumentRepository argumentRepository;

    @Transactional(readOnly = true)
    public InventiveStepAnalysis findAnalysis(Case caseEntity) {
        return analysisRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTIVE_STEP_ANALYSIS_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<InventiveArgument> findArguments(InventiveStepAnalysis analysis) {
        List<InventiveArgument> arguments = argumentRepository.findAllByAnalysis(analysis);

        if (arguments.isEmpty())
            throw new BusinessException(ErrorCode.INVENTIVE_ARGUMENT_NOT_FOUND);

        return arguments;
    }

    /**
     * Case에 기존 분석이 있으면 삭제
     * 삭제 순서: InventiveArgument → InventiveStepAnalysis (FK 관계)
     */
    @Transactional
    public void deleteExistingAnalysis(Long caseId) {
        argumentRepository.deleteAllByCaseId(caseId);
        analysisRepository.deleteAllByCaseId(caseId);
        log.info("[InventiveStep] 기존 분석 삭제 완료: caseId={}", caseId);
    }

    /**
     * InventiveStepAnalysis INSERT
     * D1, D2를 지정해서 저장
     */
    @Transactional
    public InventiveStepAnalysis saveAnalysis(Case caseEntity, PriorArt d1, PriorArt d2) {
        InventiveStepAnalysis analysis = InventiveStepAnalysis.builder()
                .caseEntity(caseEntity)
                .primaryArt(d1)
                .secondaryArt(d2)
                .build();
        analysis = analysisRepository.save(analysis);
        log.info("[InventiveStep] Analysis 저장: id={}, d1={}, d2={}",
                analysis.getId(), d1.getApplicationNumber(),
                d2 != null ? d2.getApplicationNumber() : null);
        return analysis;
    }

    /**
     * InventiveArgument INSERT (4개 카테고리 모두)
     *
     * AI가 선정한 카테고리: recommended=true, content 있음
     * 선정 안 된 카테고리: recommended=false, content 빈 Map
     */
    @Transactional
    public void saveArguments(InventiveStepAnalysis analysis,
                              Map<ArgumentType, Map<String, Object>> recommendedContents) {
        for (ArgumentType type : ArgumentType.values()) {
            Map<String, Object> content = recommendedContents.get(type);
            boolean isRecommended = content != null;

            InventiveArgument argument = InventiveArgument.builder()
                    .analysis(analysis)
                    .argumentType(type)
                    .recommended(isRecommended)
                    .content(isRecommended ? content : Map.of())
                    .build();
            argumentRepository.save(argument);
        }
        log.info("[InventiveStep] Arguments 저장: 4건 (추천 {}건, 해당없음 {}건)",
                recommendedContents.size(), 4 - recommendedContents.size());
    }

    /**
     * Case의 진보성 완료 시각 갱신
     */
    @Transactional
    public void markCaseCompleted(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));
        caseEntity.completeInventiveStepAnalysis();
    }
}