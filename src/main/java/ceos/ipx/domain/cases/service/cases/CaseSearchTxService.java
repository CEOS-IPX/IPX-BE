package ceos.ipx.domain.cases.service.cases;

import ceos.ipx.domain.analysis.inventivestep.repository.InventiveArgumentRepository;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyComparisonRepository;
import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.dto.response.SearchStatusResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.domain.cases.service.common.PriorArtMapper;
import ceos.ipx.domain.report.repository.ReportRepository;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.response.search.PythonSearchResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 검색 관련 트랜잭션 처리 Service (self-invocation 문제 해결)
 *
 * 트랜잭션 메서드:
 *   1. prepareCaseAndComponents: 검색 시작 시 (동기 요청 스레드)
 *   2. saveSearchResults:        Python 응답 후 (비동기 스레드)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchTxService {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final InventionComponentRepository componentRepository;
    private final PriorArtRepository priorArtRepository;
    private final PriorArtMapper priorArtMapper;
    private final SearchProgressService searchProgressService;

    // 재검색 시 삭제해야 할 도메인을 위한 레포지토리
    private final NoveltyAnalysisRepository noveltyAnalysisRepository;
    private final NoveltyComparisonRepository noveltyComparisonRepository;
    private final InventiveStepAnalysisRepository inventiveStepAnalysisRepository;
    private final InventiveArgumentRepository inventiveArgumentRepository;
    private final ReportRepository reportRepository;

    /**
     * Case를 생성하거나 재사용하고, 구성요소를 저장
     *
     * 재검색 케이스:
     *   - Case.resetAllStages()
     *   - 기존 하위 데이터 전체 삭제 (구성요소, 선행기술, 신규성/진보성 분석, 리포트)
     */
    @Transactional
    public Case prepareSearch(Long userId, SearchRequest request) {

        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Case caseEntity;
        SearchRequest.AdditionalInfo info = request.additionalInfo();

        if (request.caseId() == null) {
            // 새 사건 생성
            caseEntity = Case.builder()
                    .user(user)
                    .title(request.title())
                    .applicantName(request.applicantName())
                    .inventorName(request.inventorName())
                    .technicalField(request.technicalField())
                    .description(request.description())
                    .userInputIpc(request.userInputIpc() != null ? request.userInputIpc() : new ArrayList<>())
                    .priorArtReference(info != null ? info.priorArtReference() : null)
                    .differentiationNotes(info != null ? info.differentiationNotes() : null)
                    .measurementConditions(info != null ? info.measurementConditions() : null)
                    .measurementResults(info != null ? info.measurementResults() : null)
                    .build();
            caseEntity = caseRepository.save(caseEntity);
            log.info("[Search] 새 사건 생성: caseId={}", caseEntity.getId());
        } else {
            // 재검색: 기존 사건 조회 + 초기화
            caseEntity = caseRepository.findByIdAndUser(request.caseId(), user)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

            resetCaseData(caseEntity);

            // 재검색 시 case 갱신 (사용자가 새로 입력한 값으로)
            caseEntity.updateAdditionalInfo(
                    request.title(),
                    request.applicantName(),
                    request.inventorName(),
                    request.technicalField(),
                    request.description(),
                    request.userInputIpc() != null ? request.userInputIpc() : new ArrayList<>(),
                    info != null ? info.priorArtReference() : null,
                    info != null ? info.differentiationNotes() : null,
                    info != null ? info.measurementConditions() : null,
                    info != null ? info.measurementResults() : null
            );

            log.info("[Search] 재검색: caseId={} 초기화 완료", caseEntity.getId());
        }

        // 구성요소 저장
        List<SearchRequest.ComponentInput> inputs = request.components();
        for (int i = 0; i < inputs.size(); i++) {
            SearchRequest.ComponentInput input = inputs.get(i);
            InventionComponent component = InventionComponent.builder()
                    .caseEntity(caseEntity)
                    .name(input.name())
                    .description(input.description())
                    .displayOrder((short) (i + 1))
                    .build();
            componentRepository.save(component);
        }
        log.info("[Search] 구성요소 {}개 저장 완료", inputs.size());

        return caseEntity;
    }

    /**
     * 재검색 시 사건의 모든 하위 데이터 삭제
     * 삭제 순서 주의: FK 참조 관계상 자식 -> 부모
     */
    private void resetCaseData(Case caseEntity) {
        Long caseId = caseEntity.getId();

        // 완료 시각 및 keywords 초기화
        caseEntity.resetAllStages();
        caseEntity.updateKeywords(List.of());

        // 리포트 삭제
        reportRepository.deleteAllByCaseId(caseId);

        // 진보성 분석 삭제 (arguments → analysis 순)
        inventiveArgumentRepository.deleteAllByCaseId(caseId);
        inventiveStepAnalysisRepository.deleteAllByCaseId(caseId);

        // 신규성 분석 삭제 (comparisons → analyses 순)
        noveltyComparisonRepository.deleteAllByCaseId(caseId);
        noveltyAnalysisRepository.deleteAllByCaseId(caseId);

        // 선행기술 삭제
        priorArtRepository.deleteAllByCaseId(caseId);

        // 구성요소 삭제
        componentRepository.deleteAllByCaseId(caseId);
    }

    /**
     * Python 검색 응답을 DB에 저장
     *
     *   1. Case.keywords 갱신 (intent.keywords)
     *   2. PriorArt INSERT
     *   3. Case.completeSearch() 호출
     */
    @Transactional
    public void saveSearchResults(Long caseId,PythonSearchResultResponse response) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(()-> new BusinessException(ErrorCode.CASE_NOT_FOUND));

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

        // 트랜잭션 커밋 후 Redis 완료 상태 저장
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        searchProgressService.markCompleted(caseId);
                    }
                }
        );

        // 3. 검색 완료 시각 갱신
        caseEntity.completeSearch();
    }
}