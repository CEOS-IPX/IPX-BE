package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.global.python.PythonInventiveStepClient;
import ceos.ipx.global.python.dto.request.inventivestep.*;
import ceos.ipx.global.python.dto.response.inventivestep.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 진보성 분석 카테고리별 논리를 비동기로 병렬 호출하는 Service
 *
 * 각 카테고리 논리 생성은 LLM 호출
 * 4개 카테고리 모두 순차 호출하는 대신, 병렬 호출로 시간 단축
 *
 * "searchTaskExecutor" 스레드풀 재사용 (기존 검색용과 공유)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventiveStepAsyncService {

    private final PythonInventiveStepClient client;

    @Async("searchTaskExecutor")
    public CompletableFuture<PythonNumericalLimitResult> generateNumericalLimitAsync(
            PythonNumericalLimitRequest request) {
        try {
            return CompletableFuture.completedFuture(client.generateNumericalLimit(request));
        } catch (Exception e) {
            log.error("[InventiveStep][Async] 수치한정 생성 실패", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("searchTaskExecutor")
    public CompletableFuture<PythonCombinationMotivationResult> generateCombinationMotivationAsync(
            PythonCombinationMotivationRequest request) {
        try {
            return CompletableFuture.completedFuture(client.generateCombinationMotivation(request));
        } catch (Exception e) {
            log.error("[InventiveStep][Async] 복수인용발명결합 생성 실패", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("searchTaskExecutor")
    public CompletableFuture<PythonCommonTechniqueResult> generateCommonTechniqueAsync(
            PythonCommonTechniqueRequest request) {
        try {
            return CompletableFuture.completedFuture(client.generateCommonTechnique(request));
        } catch (Exception e) {
            log.error("[InventiveStep][Async] 주지관용기술 생성 실패", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("searchTaskExecutor")
    public CompletableFuture<PythonSimpleDesignResult> generateSimpleDesignAsync(
            PythonSimpleDesignRequest request) {
        try {
            return CompletableFuture.completedFuture(client.generateSimpleDesign(request));
        } catch (Exception e) {
            log.error("[InventiveStep][Async] 단순설계변경 생성 실패", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}