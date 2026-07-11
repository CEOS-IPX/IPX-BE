package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.analysis.inventivestep.repository.InventiveArgumentRepository;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyComparisonRepository;
import ceos.ipx.domain.cases.dto.request.SearchRequest;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.domain.report.repository.ReportRepository;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchTxService {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final InventionComponentRepository componentRepository;
    private final PriorArtRepository priorArtRepository;

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
     *   - Case.resetAllStages() 호출
     *   - 기존 구성요소, 선행기술, 분석 결과, 리포트 모두 삭제
     */
    @Transactional
    public Case prepareCaseAndComponents(Long userId, SearchRequest request) {
        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Case caseEntity;

        if (request.caseId() == null) {
            // 새 사건 생성
            caseEntity = Case.builder()
                    .user(user)
                    .title(request.title())
                    .applicantName(request.applicantName())
                    .inventorName(request.inventorName())
                    .technicalField(request.technicalField())
                    .description(request.description())
                    .userInputIpc(request.userInputIpc())
                    .build();
            caseEntity = caseRepository.save(caseEntity);
            log.info("[Search] 새 사건 생성: caseId={}", caseEntity.getId());
        } else {
            // 재검색: 기존 사건 조회 + 초기화
            caseEntity = caseRepository.findByIdAndUser(request.caseId(), user)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

            resetCaseData(caseEntity);
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
     * 재검색 시 Case의 모든 하위 데이터 삭제
     * 삭제 순서: FK 참조 관계상 자식 -> 부모
     */
    private void resetCaseData(Case caseEntity) {
        Long caseId = caseEntity.getId();

        // 완료 시각 초기화
        caseEntity.resetAllStages();
        caseEntity.updateKeywords(List.of());

        // 리포트 삭제 (독립 테이블)
        reportRepository.deleteAllByCaseId(caseId);

        // 진보성 분석 삭제 (arguments가 analysis를 참조하므로 arguments부터)
        inventiveArgumentRepository.deleteAllByCaseId(caseId);
        inventiveStepAnalysisRepository.deleteAllByCaseId(caseId);

        // 신규성 분석 삭제 (comparisons가 analyses를 참조)
        noveltyComparisonRepository.deleteAllByCaseId(caseId);
        noveltyAnalysisRepository.deleteAllByCaseId(caseId);

        // 선행기술 삭제
        priorArtRepository.deleteAllByCaseId(caseId);

        // 구성요소 삭제
        componentRepository.deleteAllByCaseId(caseId);
    }
}