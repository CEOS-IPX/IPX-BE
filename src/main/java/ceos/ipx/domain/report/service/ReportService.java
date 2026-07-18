package ceos.ipx.domain.report.service;

import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.report.dto.request.ReportCreateRequest;
import ceos.ipx.domain.report.dto.response.ReportCreateResponse;
import ceos.ipx.domain.report.entity.Report;
import ceos.ipx.domain.report.repository.ReportRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final CaseRepository caseRepository;
    private final NoveltyAnalysisRepository noveltyAnalysisRepository;
    private final InventiveStepAnalysisRepository inventiveStepAnalysisRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public ReportSaveResult saveReport(
            Long userId,
            Long caseId,
            ReportCreateRequest request
    ) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        validateCaseOwner(caseEntity, userId);
        validateAnalysisResults(caseEntity);

        Optional<Report> existingReport =
                reportRepository.findByCaseEntity(caseEntity);

        boolean created;

        Report report;

        if (existingReport.isPresent()) {
            if (!Boolean.TRUE.equals(request.overwrite())) {
                throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
            }

            report = existingReport.get();

            report.update(
                    request.authorName(),
                    request.noveltySatisfied(),
                    request.inventiveSatisfied(),
                    request.overallConclusion()
            );

            created = false;
        } else {
            report = Report.builder()
                    .caseEntity(caseEntity)
                    .authorName(request.authorName())
                    .noveltySatisfied(request.noveltySatisfied())
                    .inventiveSatisfied(request.inventiveSatisfied())
                    .overallConclusion(request.overallConclusion())
                    .build();

            reportRepository.save(report);

            created = true;
        }

        caseEntity.completeReport();

        reportRepository.flush();
        caseRepository.flush();

        ReportCreateResponse response =
                ReportCreateResponse.of(report, caseEntity);

        return new ReportSaveResult(response, created);
    }

    private void validateCaseOwner(Case caseEntity, Long userId) {
        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }
    }

    private void validateAnalysisResults(Case caseEntity) {
        noveltyAnalysisRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.NOVELTY_ANALYSIS_NOT_FOUND
                        )
                );

        inventiveStepAnalysisRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVENTIVE_STEP_ANALYSIS_NOT_FOUND
                        )
                );
    }
}