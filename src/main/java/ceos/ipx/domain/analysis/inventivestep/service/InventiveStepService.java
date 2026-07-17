package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.analysis.inventivestep.dto.response.InventiveStepResponse;
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

        // 1. 사전 조회
        Case caseEntity = caseQueryTxService.findCaseWithAuth(userId, caseId);
        List<InventionComponent> components = caseQueryTxService.findComponents(caseEntity);
        List<PriorArt> priorArts = caseQueryTxService.findPriorArts(caseEntity);

        // 2. D1 확정
        PriorArt d1 = findD1(priorArts, primaryApplicationNumber);

        // 3. D2 후보 준비 (D1 제외 상위 N건)
        List<PriorArt> d2Candidates = priorArts.stream()
                .filter(pa -> !pa.getId().equals(d1.getId()))
                .limit(D2_CANDIDATE_LIMIT)
                .toList();

        if (d2Candidates.isEmpty()) {
            log.error("[InventiveStep] D2 후보 없음: caseId={}", caseId);
            throw new BusinessException(ErrorCode.INVENTIVE_STEP_NO_CANDIDATES);
        }

        // 4. OpenSearch mget으로 D1 + 후보들의 청구항/초록 조회
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

        // 5. Python 호출 - D2 선정
        PythonPriorArtInfo d1Info = mapper.toPythonPriorArtInfo(d1, d1Doc);
        List<PythonPriorArtInfo> candidateInfos = d2Candidates.stream()
                .map(pa -> mapper.toPythonPriorArtInfo(pa, docMap.get(pa.getApplicationNumber())))
                .toList();
        List<PythonInventionComponent> pyComponents = mapper.toPythonComponents(components);

        PythonSelectSecondaryResponse secondaryResp = pythonClient.selectSecondary(
                PythonSelectSecondaryRequest.builder()
                        .inventionTitle(caseEntity.getTitle())
                        .inventionDescription(caseEntity.getDescription())
                        .components(pyComponents)
                        .primaryArt(d1Info)
                        .candidates(candidateInfos)
                        .build()
        );

        // 6. D2 확정 (PriorArt 및 PatentDocument)
        String d2AppNum = secondaryResp.d2ApplicationNumber();
        PriorArt d2 = d2Candidates.stream()
                .filter(pa -> pa.getApplicationNumber().equals(d2AppNum))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[InventiveStep] Python이 반환한 D2가 후보에 없음: {}", d2AppNum);
                    return new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
                });
        PatentDocument d2Doc = docMap.get(d2AppNum);
        PythonPriorArtInfo d2Info = mapper.toPythonPriorArtInfo(d2, d2Doc);

        // 7. Python 호출 - 카테고리 선정
        PythonSelectCategoriesResponse categoriesResp = pythonClient.selectCategories(
                buildSelectCategoriesRequest(caseEntity, pyComponents, d1Info, d2Info)
        );

        Set<ArgumentType> selectedCategories = categoriesResp.categories().stream()
                .map(ArgumentTypeConverter::fromPython)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (selectedCategories.isEmpty()) {
            log.error("[InventiveStep] 유효한 카테고리 없음: raw={}", categoriesResp.categories());
            throw new BusinessException(ErrorCode.PYTHON_SERVER_ERROR);
        }

        log.info("[InventiveStep] 선정 카테고리: {}", selectedCategories);

        // 8. 선정된 카테고리별로 병렬 호출
        Map<ArgumentType, Map<String, Object>> recommendedContents = generateArgumentsInParallel(
                selectedCategories, caseEntity, pyComponents, d1Info, d2Info);

        // 9. DB 저장 (기존 삭제 후 새로 생성)
        txService.deleteExistingAnalysis(caseId);
        InventiveStepAnalysis analysis = txService.saveAnalysis(caseEntity, d1, d2);
        txService.saveArguments(analysis, recommendedContents);
        txService.markCaseCompleted(caseId);

        log.info("[InventiveStep] 완료: caseId={}, recommended={}, notRecommended={}",
                caseId, recommendedContents.size(), 4 - recommendedContents.size());

        // 10. 응답 조립
        return InventiveStepResponse.of(analysis, d1, d2, recommendedContents);
    }

    public InventiveStepResponse getAnalysis(Long userId, Long caseId) {
        // 1. Case 조회 (권한 검증)
        Case caseEntity = caseQueryTxService.findCaseWithAuth(userId, caseId);

        // 2. 저장된 분석 조회
        InventiveStepAnalysis analysis = txService.findAnalysis(caseEntity);

        // 3. Arguments 조회 (4개 카테고리 모두)
        List<InventiveArgument> argumentEntities = txService.findArguments(analysis);

        // 4. 응답 조립
        return InventiveStepResponse.ofEntities(analysis, argumentEntities);
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
            Set<ArgumentType> selectedCategories, Case caseEntity,
            List<PythonInventionComponent> pyComponents,
            PythonPriorArtInfo d1Info, PythonPriorArtInfo d2Info) {

        Map<ArgumentType, CompletableFuture<Map<String, Object>>> futures = new EnumMap<>(ArgumentType.class);

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

                case COMBINATION_MOTIVATION -> futures.put(type,
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

        // 모든 병렬 작업 완료 대기
        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.warn("[InventiveStep] 일부 카테고리 생성 실패 (계속 진행)", ex);
                    return null;
                })
                .join();

        // 성공한 것만 결과 수집
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

        return results;
    }
}