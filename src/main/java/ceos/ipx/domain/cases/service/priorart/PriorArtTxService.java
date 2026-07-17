package ceos.ipx.domain.cases.service.priorart;

import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.PriorArtRepository;
import ceos.ipx.domain.cases.service.common.PriorArtMapper;
import ceos.ipx.domain.report.repository.ReportRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.dto.response.search.PythonAddManualResponse;
import ceos.ipx.global.python.dto.response.search.PythonSearchResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 선행기술 결과 관리 트랜잭션 Service (self-invocation 문제 해결)
 *
 * 담당 트랜잭션:
 *   - 중복 필터링, context 조립
 *   - 수동 추가 결과 저장 (PriorArt INSERT)
 *   - 선행문헌 분석 포함 여부 수정
 *   - 선행문헌 삭제
 *
 * Case 조회(권한 검증), 전체 PriorArt 조회는 CaseQueryTxService에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriorArtTxService {

    private final CaseRepository caseRepository;
    private final PriorArtRepository priorArtRepository;
    private final PriorArtMapper priorArtMapper;
    private final NoveltyAnalysisRepository noveltyAnalysisRepository;
    private final InventiveStepAnalysisRepository inventiveStepAnalysisRepository;
    private final ReportRepository reportRepository;

    /**
     * 선행문헌 단건 조회 및 사건 소유권 검증
     */
    @Transactional(readOnly = true)
    public PriorArt findPriorArtWithAuth(Long userId, Long priorArtId) {
        PriorArt priorArt = priorArtRepository.findById(priorArtId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRIOR_ART_NOT_FOUND));

        if (!priorArt.getCaseEntity().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        return priorArt;
    }

    /**
     * 선행문헌 삭제
     *
     * 삭제 대상 선행문헌을 참조하는 분석이 존재하면
     * DB ON DELETE CASCADE에 따라 관련 분석과 하위 결과가 함께 삭제된다.
     * 이 경우 사건 단계 완료 상태와 기존 리포트도 무효화한다.
     *
     * @return 삭제된 선행문헌이 속했던 사건 ID
     */
    @Transactional
    public Long deletePriorArt(Long userId, Long priorArtId) {
        PriorArt priorArt = findPriorArtWithAuth(userId, priorArtId);

        Case caseEntity = priorArt.getCaseEntity();
        Long caseId = caseEntity.getId();

        boolean usedInNovelty =
                noveltyAnalysisRepository.existsByD1PriorArt_Id(priorArtId);

        boolean usedInInventive =
                inventiveStepAnalysisRepository
                        .existsByPrimaryArt_IdOrSecondaryArt_Id(
                                priorArtId,
                                priorArtId
                        );

        if (usedInNovelty) {
            caseEntity.resetNoveltyAnalysis();
        }

        if (usedInInventive) {
            caseEntity.resetInventiveStepAnalysis();
        }

        if (usedInNovelty || usedInInventive) {
            reportRepository.deleteAllByCaseId(caseId);
            caseEntity.resetReport();
        }

        priorArtRepository.delete(priorArt);

        return caseId;
    }

    /**
     * 중복 확인: 이미 존재하는 출원번호 리스트 반환
     */
    @Transactional(readOnly = true)
    public Set<String> findDuplicateApplicationNumbers(
            Case caseEntity,
            List<String> applicationNumbers
    ) {
        return priorArtRepository.findExistingApplicationNumbers(
                caseEntity,
                applicationNumbers
        );
    }

    /**
     * Python이 반환한 새 특허 정보를 prior_arts에 INSERT
     */
    @Transactional
    public void saveManualPriorArts(
            Long caseId,
            PythonAddManualResponse response
    ) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        List<PythonSearchResultResponse.PatentResult> results = response.results();

        if (results == null || results.isEmpty()) {
            log.info("[AddManual] 저장할 새 특허 없음: caseId={}", caseId);
            return;
        }

        for (PythonSearchResultResponse.PatentResult result : results) {
            PriorArt priorArt = priorArtMapper.toEntity(caseEntity, result);
            priorArtRepository.save(priorArt);
        }

        log.info(
                "[AddManual] {}건 저장 완료: caseId={}",
                results.size(),
                caseId
        );
    }
}