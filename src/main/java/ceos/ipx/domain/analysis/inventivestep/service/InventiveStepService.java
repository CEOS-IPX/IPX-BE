package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.analysis.inventivestep.dto.response.InventiveStepResponse;
import ceos.ipx.domain.cases.dto.analysis.CaseAnalysisContext;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.service.common.CaseQueryTxService;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.opensearch.OpenSearchClient;
import ceos.ipx.global.opensearch.dto.PatentDocument;
import ceos.ipx.global.python.PythonInventiveStepClient;
import ceos.ipx.global.python.dto.request.inventivestep.*;
import ceos.ipx.global.python.dto.response.inventivestep.*;
import ceos.ipx.global.python.dto.common.PythonInventionComponent;
import ceos.ipx.global.python.dto.common.PythonPriorArtInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 진보성 분석 서비스
 *
 * 흐름:
 *   1. Case + Components + PriorArts 조회 (권한 검증)
 *   2. D1 확정, D2 후보 준비 (D1 제외 상위 N건)
 *   3. OpenSearch mget으로 D1 + 후보들의 청구항/초록 조회
 *   4. Python /select-secondary → D2 결정
 *   5. Python /select-categories → 선정된 카테고리 리스트
 *   6. 선정된 카테고리별로 Python /generate 병렬 호출
 *   7. DB 저장 (기존 삭제 후 새로 생성)
 *   8. 응답 조립
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventiveStepService {

    /** D2 후보 개수 (D1 제외 상위 N건) */
    private static final int D2_CANDIDATE_LIMIT = 5;

    private final InventiveStepTxService txService;
    private final CaseQueryTxService caseQueryTxService;
    private final InventiveStepAsyncService asyncService;
    private final PythonInventiveStepClient pythonClient;
    private final OpenSearchClient openSearchClient;
    private final InventiveStepMapper mapper;

    /**
     * 진보성 분석 실행
     */
    public InventiveStepResponse analyze(Long userId, Long caseId, String primaryApplicationNumber) {
        log.info("[InventiveStep] 시작: caseId={}, d1={}", caseId, primaryApplicationNumber);

        // ============================================================
        // 1. 사전 조회
        // ============================================================
        CaseAnalysisContext context = caseQueryTxService.getAnalysisContext(userId, caseId);

        Case caseEntity = context.caseEntity();
        List<InventionComponent> components = context.components();
        List<PriorArt> priorArts = context.priorArts();

        // ============================================================
        // 2. D1 확정
        // ============================================================
        PriorArt d1 = findD1(priorArts, primaryApplicationNumber);

        // ============================================================
        // 3. D2 후보 준비 (D1 제외 상위 N건)
        // ============================================================
        List<PriorArt> d2Candidates = priorArts.stream()
                .filter(pa -> !pa.getId().equals(d1.getId()))
                .limit(D2_CANDIDATE_LIMIT)
                .toList();

        if (d2Candidates.isEmpty()) {
            // D2 후보 자체가 없는 경우 - D1 단독 분석으로 진행 가능
            log.warn("[InventiveStep] D2 후보 없음: caseId={}. D1 단독 분석으로 진행.", caseId);
        }

        // ============================================================
        // 4. OpenSearch mget으로 D1 + 후보들의 청구항/초록 조회
        // ============================================================
        List<String> appNums = new ArrayList<>();
        appNums.add(d1.getApplicationNumber());
        d2Candidates.forEach(pa -> appNums.add(pa.getApplicationNumber()));

        List<PatentDocument> documents = openSearchClient.mgetByApplicationNumbers(appNums);
        Map<String, PatentDocument> docMap = documents.stream()
                .collect(Collectors.toMap(PatentDocument::applicationNumber, d -> d));

        PatentDocument d1Doc = docMap.get(d1.getApplicationNumber());
        if (d1Doc == null) {
            log.error("[InventiveStep] D1의 OpenSearch 문서 없음: {}", d1.getApplicationNumber());
            throw new BusinessException(ErrorCode.PRIOR_ART_DOCUMENT_NOT_FOUND);
        }

        // ============================================================
        // 5. Python 호출 - D2 선정 (후보 있을 때만)
        // ============================================================
        PythonPriorArtInfo d1Info = mapper.toPythonPriorArtInfo(d1, d1Doc);
        List<PythonInventionComponent> pyComponents = mapper.toPythonComponents(components);

        PriorArt d2 = null;
        PythonPriorArtInfo d2Info = null;

        if (!d2Candidates.isEmpty()) {
            List<PythonPriorArtInfo> candidateInfos = d2Candidates.stream()
                    .map(pa -> mapper.toPythonPriorArtInfo(pa, docMap.get(pa.getApplicationNumber())))
                    .toList();

            PythonSelectSecondaryResponse secondaryResp = pythonClient.selectSecondary(
                    PythonSelectSecondaryRequest.builder()
                            .inventionTitle(caseEntity.getTitle())
                            .inventionDescription(caseEntity.getDescription())
                            .components(pyComponents)
                            .primaryArt(d1Info)
                            .candidates(candidateInfos)
                            .build()
            );

            String d2AppNum = secondaryResp.d2ApplicationNumber();

            // ============================================================
            // 6. D2 확정 (null 대응)
            // ============================================================
            if (d2AppNum != null && !d2AppNum.isBlank()) {
                d2 = d2Candidates.stream()
                        .filter(pa -> pa.getApplicationNumber().equals(d2AppNum))
                        .findFirst()
                        .orElse(null);

                if (d2 == null) {
                    // Python이 후보에 없는 번호 반환 (오작동)
                    log.warn(
                            "[InventiveStep] Python이 반환한 D2가 후보에 없음: {}. D1 단독 분석으로 진행.",
                            d2AppNum
                    );
                } else {
                    PatentDocument d2Doc = docMap.get(d2AppNum);
                    if (d2Doc == null) {
                        log.warn(
                                "[InventiveStep] D2의 OpenSearch 문서 없음: {}. D1 단독 분석으로 진행.",
                                d2AppNum
                        );
                        d2 = null;  // D2 사용 불가로 처리
                    } else {
                        d2Info = mapper.toPythonPriorArtInfo(d2, d2Doc);
                        log.info(
                                "[InventiveStep] D2 선정: {}",
                                d2.getApplicationNumber()
                        );
                    }
                }
            } else {
                // Python이 명시적으로 D2 없음 반환 (정상 케이스)
                log.info(
                        "[InventiveStep] Python이 D2 미선정. D1 단독 분석 진행."
                );
            }
        }

        // ============================================================
        // 7. Python 호출 - 카테고리 선정 (D2 Optional 전달)
        // ============================================================
        PythonSelectCategoriesResponse categoriesResp = pythonClient.selectCategories(
                buildSelectCategoriesRequest(caseEntity, pyComponents, d1Info, d2Info)
        );

        Set<ArgumentType> selectedCategories = categoriesResp.categories().stream()
                .map(ArgumentTypeConverter::fromPython)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // ============================================================
        // 7-1. D2 없으면 COMBINATION_MOTIVATION 강제 제외 (방어 코드)
        // ============================================================
        if (d2 == null && selectedCategories.contains(ArgumentType.COMBINATION_MOTIVATION)) {
            log.warn(
                    "[InventiveStep] D2 없는데 COMBINATION_MOTIVATION 선정됨. 자동 제외."
            );
            selectedCategories.remove(ArgumentType.COMBINATION_MOTIVATION);
        }

        if (selectedCategories.isEmpty()) {
            log.error(
                    "[InventiveStep] 유효한 카테고리 없음: raw={}, d2={}",
                    categoriesResp.categories(),
                    d2 != null ? d2.getApplicationNumber() : "null"
            );
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info(
                "[InventiveStep] 선정 카테고리: {}, D2 상태: {}",
                selectedCategories,
                d2 != null ? "있음" : "없음 (D1 단독)"
        );

        // ============================================================
        // 8. 선정된 카테고리별로 병렬 호출 (D2 Optional 전달)
        // ============================================================
        Map<ArgumentType, Map<String, Object>> recommendedContents = generateArgumentsInParallel(
                selectedCategories, caseEntity, pyComponents, d1Info, d2Info
        );

        // ============================================================
        // 9. DB 저장 (D2 nullable 전달)
        // ============================================================
        return txService.saveAll(caseId, d1, d2, recommendedContents);
    }

    @Transactional(readOnly = true)
    public InventiveStepResponse getAnalysis(Long userId, Long caseId) {
        // 1. Case 조회 (권한 검증)
        Case caseEntity = caseQueryTxService.findCaseWithAuth(userId, caseId);

        // 2. 저장된 분석 조회
        // 3. Arguments 조회 (4개 카테고리 모두)
        // 4. 응답 조립
        return txService.getAnalysisResponse(caseEntity);
    }

    private PriorArt findD1(List<PriorArt> priorArts, String primaryApplicationNumber) {
        return priorArts.stream()
                .filter(pa -> pa.getApplicationNumber().equals(primaryApplicationNumber))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[InventiveStep] D1이 사건의 선행기술에 없음: {}", primaryApplicationNumber);
                    return new BusinessException(ErrorCode.PRIOR_ART_NOT_FOUND);
                });
    }

    private PythonSelectCategoriesRequest buildSelectCategoriesRequest(
            Case caseEntity, List<PythonInventionComponent> pyComponents,
            PythonPriorArtInfo d1Info, PythonPriorArtInfo d2Info) {
        return PythonSelectCategoriesRequest.builder()
                .inventionTitle(caseEntity.getTitle())
                .inventionDescription(caseEntity.getDescription())
                .components(pyComponents)
                .primaryArt(d1Info)
                .secondaryArt(d2Info)
                .priorArtReference(caseEntity.getPriorArtReference())
                .differentiationNotes(caseEntity.getDifferentiationNotes())
                .measurementConditions(caseEntity.getMeasurementConditions())
                .measurementResults(caseEntity.getMeasurementResults())
                .build();
    }

    /**
     * 선정된 카테고리별로 Python /generate를 병렬 호출
     * 실패한 카테고리는 결과에서 제외 (다른 카테고리는 계속 진행)
     */
    private Map<ArgumentType, Map<String, Object>> generateArgumentsInParallel(
            Set<ArgumentType> selectedCategories,
            Case caseEntity,
            List<PythonInventionComponent> pyComponents,
            PythonPriorArtInfo d1Info,
            PythonPriorArtInfo d2Info  // nullable
    ) {
        Map<ArgumentType, CompletableFuture<Map<String, Object>>> futures =
                new EnumMap<>(ArgumentType.class);

        // ============================================================
        // 카테고리별 병렬 태스크 등록
        // ============================================================
        for (ArgumentType type : selectedCategories) {
            switch (type) {
                case NUMERICAL_LIMIT -> futures.put(type,
                        asyncService.generateNumericalLimitAsync(
                                PythonNumericalLimitRequest.builder()
                                        .inventionTitle(caseEntity.getTitle())
                                        .inventionDescription(caseEntity.getDescription())
                                        .primaryArt(d1Info)
                                        .measurementConditions(caseEntity.getMeasurementConditions())
                                        .measurementResults(caseEntity.getMeasurementResults())
                                        .build()
                        ).thenApply(mapper::toContentMap));

                case COMBINATION_MOTIVATION -> {
                    // D2 필수 카테고리 - d2Info null이면 스킵 (이중 방어)
                    if (d2Info == null) {
                        log.warn(
                                "[InventiveStep] COMBINATION_MOTIVATION은 D2 필수인데 d2Info=null. " +
                                        "카테고리 스킵. analyze() 로직 재검토 필요."
                        );
                        // 이 카테고리는 등록하지 않음 (결과에서 자동 제외됨)
                        continue;
                    }
                    futures.put(type,
                            asyncService.generateCombinationMotivationAsync(
                                    PythonCombinationMotivationRequest.builder()
                                            .inventionTitle(caseEntity.getTitle())
                                            .inventionDescription(caseEntity.getDescription())
                                            .primaryArt(d1Info)
                                            .secondaryArt(d2Info)
                                            .priorArtReference(caseEntity.getPriorArtReference())
                                            .differentiationNotes(caseEntity.getDifferentiationNotes())
                                            .build()
                            ).thenApply(mapper::toContentMap));
                }

                case COMMON_TECHNIQUE -> futures.put(type,
                        asyncService.generateCommonTechniqueAsync(
                                PythonCommonTechniqueRequest.builder()
                                        .inventionTitle(caseEntity.getTitle())
                                        .inventionDescription(caseEntity.getDescription())
                                        .components(pyComponents)
                                        .primaryArt(d1Info)
                                        .priorArtReference(caseEntity.getPriorArtReference())
                                        .differentiationNotes(caseEntity.getDifferentiationNotes())
                                        .build()
                        ).thenApply(mapper::toContentMap));

                case SIMPLE_DESIGN -> futures.put(type,
                        asyncService.generateSimpleDesignAsync(
                                PythonSimpleDesignRequest.builder()
                                        .inventionTitle(caseEntity.getTitle())
                                        .inventionDescription(caseEntity.getDescription())
                                        .components(pyComponents)
                                        .primaryArt(d1Info)
                                        .priorArtReference(caseEntity.getPriorArtReference())
                                        .differentiationNotes(caseEntity.getDifferentiationNotes())
                                        .build()
                        ).thenApply(mapper::toContentMap));
            }
        }

        // ============================================================
        // 모든 병렬 태스크 등록 완료 확인
        // ============================================================
        if (futures.isEmpty()) {
            log.error("[InventiveStep] 병렬 실행할 카테고리가 없음. D2 없음 + COMBINATION_MOTIVATION 단독 선정 가능성.");
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        // ============================================================
        // 모든 병렬 작업 완료 대기 (for 루프 종료 후)
        // ============================================================
        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.warn("[InventiveStep] 일부 카테고리 생성 실패 (계속 진행)", ex);
                    return null;
                })
                .join();

        // ============================================================
        // 성공한 것만 결과 수집
        // ============================================================
        Map<ArgumentType, Map<String, Object>> results = new EnumMap<>(ArgumentType.class);
        for (Map.Entry<ArgumentType, CompletableFuture<Map<String, Object>>> entry : futures.entrySet()) {
            try {
                Map<String, Object> content = entry.getValue().get();
                if (content != null) {
                    results.put(entry.getKey(), content);
                }
            } catch (Exception e) {
                log.warn("[InventiveStep] 카테고리 {} 결과 수집 실패", entry.getKey(), e);
            }
        }

        if (results.isEmpty()) {
            log.error("[InventiveStep] 모든 카테고리 실패");
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info(
                "[InventiveStep] 병렬 생성 완료: 성공 {}/{} 카테고리 ({})",
                results.size(),
                futures.size(),
                results.keySet()
        );

        return results;
    }
}