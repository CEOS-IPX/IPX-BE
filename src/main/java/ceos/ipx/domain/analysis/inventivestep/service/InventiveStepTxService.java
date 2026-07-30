package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.domain.analysis.inventivestep.dto.response.InventiveStepResponse;
import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveArgumentRepository;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.report.repository.ReportRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public InventiveStepResponse getAnalysisResponse(Case caseEntity) {
        InventiveStepAnalysis analysis = analysisRepository.findByCaseEntityWithArts(caseEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTIVE_STEP_ANALYSIS_NOT_FOUND));

        List<InventiveArgument> argumentEntities = argumentRepository.findAllByAnalysis(analysis);

        return InventiveStepResponse.ofEntities(analysis, argumentEntities);
    }

    @Transactional
    public InventiveStepResponse saveAll(
            Long caseId,
            PriorArt d1,
            PriorArt d2,
            Map<ArgumentType, Map<String, Object>> recommendedContents
    ) {
        // 1. 기존 삭제
        deleteExistingAnalysis(caseId);

        // 2. Case managed 상태로 조회
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        // 3. Analysis 저장
        InventiveStepAnalysis analysis = saveAnalysis(caseEntity, d1, d2);

        // 4. Arguments 저장
        List<InventiveArgument> arguments = saveArguments(analysis,recommendedContents);

        // 5. Case 완료 시각 갱신 (managed 엔티티, dirty checking 작동)
        caseEntity.completeInventiveStepAnalysis();

        log.info("[InventiveStep] 저장 완료: caseId={}, analysisId={}, recommended={}",
                caseId, analysis.getId(), recommendedContents.size());

        // 6. 응답 조립
        return InventiveStepResponse.ofEntities(analysis, arguments);
    }

    /**
     * Case에 기존 분석이 있으면 삭제
     * 삭제 순서: InventiveArgument → InventiveStepAnalysis (FK 관계)
     */
    private void deleteExistingAnalysis(Long caseId) {
        argumentRepository.deleteAllByCaseId(caseId);
        analysisRepository.deleteAllByCaseId(caseId);
        reportRepository.deleteAllByCaseId(caseId);

        log.info("[InventiveStep] 기존 분석 삭제 완료: caseId={}", caseId);
    }

    /**
     * InventiveStepAnalysis INSERT
     * D1, D2를 지정해서 저장
     */
    private InventiveStepAnalysis saveAnalysis(Case caseEntity, PriorArt d1, PriorArt d2) {
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
    private List<InventiveArgument> saveArguments(
            InventiveStepAnalysis analysis,
            Map<ArgumentType, Map<String, Object>> recommendedContents
    ) {
        List<InventiveArgument> arguments = Arrays.stream(ArgumentType.values())
                .map(type -> {
                    Map<String, Object> content = recommendedContents.get(type);
                    boolean isRecommended = content != null;
                    return InventiveArgument.builder()
                            .analysis(analysis)
                            .argumentType(type)
                            .recommended(isRecommended)
                            .content(isRecommended ? content : Map.of())
                            .build();
                })
                .toList();

        List<InventiveArgument> savedList = argumentRepository.saveAll(arguments);

        log.info("[InventiveStep] Arguments 저장: 4건 (추천 {}건, 해당없음 {}건)",
                recommendedContents.size(), 4 - recommendedContents.size());

        return savedList;
    }
}