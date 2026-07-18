package ceos.ipx.domain.report.service;

import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveArgumentRepository;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyComparisonRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.domain.report.dto.request.ReportCreateRequest;
import ceos.ipx.domain.report.dto.request.ReportUpdateRequest;
import ceos.ipx.domain.report.dto.response.ReportCreateResponse;
import ceos.ipx.domain.report.dto.response.ReportDetailResponse;
import ceos.ipx.domain.report.dto.response.ReportUpdateResponse;
import ceos.ipx.domain.report.entity.Report;
import ceos.ipx.domain.report.repository.ReportRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final CaseRepository caseRepository;
    private final InventionComponentRepository inventionComponentRepository;
    private final NoveltyAnalysisRepository noveltyAnalysisRepository;
    private final NoveltyComparisonRepository noveltyComparisonRepository;
    private final InventiveStepAnalysisRepository inventiveStepAnalysisRepository;
    private final InventiveArgumentRepository inventiveArgumentRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public ReportCreateResponse saveReport(
            Long userId,
            Long caseId,
            ReportCreateRequest request
    ) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CASE_NOT_FOUND)
                );

        validateCaseOwner(caseEntity, userId);
        validateAnalysisResults(caseEntity);

        if (reportRepository.findByCaseEntity(caseEntity).isPresent()) {
            throw new BusinessException(
                    ErrorCode.REPORT_ALREADY_EXISTS
            );
        }

        Report report = Report.builder()
                .caseEntity(caseEntity)
                .authorName(request.authorName().trim())
                .noveltySatisfied(request.noveltySatisfied())
                .inventiveSatisfied(request.inventiveSatisfied())
                .overallConclusion(request.overallConclusion().trim())
                .build();

        reportRepository.save(report);

        caseEntity.completeReport();

        reportRepository.flush();
        caseRepository.flush();

        return ReportCreateResponse.of(report, caseEntity);
    }

    @Transactional
    public ReportUpdateResponse updateReport(
            Long userId,
            Long caseId,
            ReportUpdateRequest request
    ) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CASE_NOT_FOUND)
                );

        validateCaseOwner(caseEntity, userId);

        Report report = reportRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.REPORT_NOT_FOUND)
                );

        report.update(
                request.authorName(),
                request.noveltySatisfied(),
                request.inventiveSatisfied(),
                request.overallConclusion()
        );

        reportRepository.flush();

        return ReportUpdateResponse.of(report, caseEntity);
    }

    public ReportDetailResponse getReport(
            Long userId,
            Long caseId
    ) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CASE_NOT_FOUND)
                );

        validateCaseOwner(caseEntity, userId);

        Report report = reportRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.REPORT_NOT_FOUND)
                );

        NoveltyAnalysis noveltyAnalysis =
                noveltyAnalysisRepository.findByCaseEntity(caseEntity)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.NOVELTY_ANALYSIS_NOT_FOUND
                                )
                        );

        InventiveStepAnalysis inventiveStepAnalysis =
                inventiveStepAnalysisRepository
                        .findByCaseEntityWithArts(caseEntity)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVENTIVE_STEP_ANALYSIS_NOT_FOUND
                                )
                        );

        List<InventionComponent> components =
                inventionComponentRepository
                        .findByCaseEntityOrderByDisplayOrderAsc(
                                caseEntity
                        );

        List<NoveltyComparison> comparisons =
                noveltyComparisonRepository
                        .findAllByAnalysis(noveltyAnalysis);

        List<InventiveArgument> arguments =
                inventiveArgumentRepository
                        .findAllByAnalysis(inventiveStepAnalysis)
                        .stream()
                        .sorted(
                                Comparator.comparingInt(
                                        argument ->
                                                argument
                                                        .getArgumentType()
                                                        .ordinal()
                                )
                        )
                        .toList();

        return ReportDetailResponse.builder()
                .reportId(report.getId())
                .caseId(caseEntity.getId())
                .caseTitle(caseEntity.getTitle())
                .applicantName(caseEntity.getApplicantName())
                .inventorName(caseEntity.getInventorName())
                .technicalField(caseEntity.getTechnicalField())
                .description(caseEntity.getDescription())
                .authorName(report.getAuthorName())
                .noveltySatisfied(report.getNoveltySatisfied())
                .inventiveSatisfied(report.getInventiveSatisfied())
                .overallConclusion(report.getOverallConclusion())
                .components(
                        components.stream()
                                .map(this::toComponentResponse)
                                .toList()
                )
                .noveltyAnalysis(
                        toNoveltyAnalysisResponse(
                                noveltyAnalysis,
                                comparisons
                        )
                )
                .inventiveStepAnalysis(
                        toInventiveStepAnalysisResponse(
                                inventiveStepAnalysis,
                                arguments
                        )
                )
                .reportCompletedAt(
                        caseEntity.getReportCompletedAt()
                )
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private ReportDetailResponse.ComponentResponse
    toComponentResponse(
            InventionComponent component
    ) {
        return ReportDetailResponse.ComponentResponse.builder()
                .componentId(component.getId())
                .name(component.getName())
                .description(component.getDescription())
                .displayOrder(component.getDisplayOrder())
                .build();
    }

    private ReportDetailResponse.NoveltyAnalysisResponse
    toNoveltyAnalysisResponse(
            NoveltyAnalysis analysis,
            List<NoveltyComparison> comparisons
    ) {
        return ReportDetailResponse.NoveltyAnalysisResponse.builder()
                .analysisId(analysis.getId())
                .overallSimilarity(
                        analysis.getOverallSimilarity().name()
                )
                .overallSimilarityLabel(
                        analysis.getOverallSimilarity().getLabel()
                )
                .conclusionText(analysis.getConclusionText())
                .primaryPriorArt(
                        toPriorArtSummary(
                                analysis.getD1PriorArt()
                        )
                )
                .comparisons(
                        comparisons.stream()
                                .map(
                                        this::
                                                toNoveltyComparisonResponse
                                )
                                .toList()
                )
                .build();
    }

    private ReportDetailResponse.NoveltyComparisonResponse
    toNoveltyComparisonResponse(
            NoveltyComparison comparison
    ) {
        InventionComponent component =
                comparison.getComponent();

        return ReportDetailResponse.NoveltyComparisonResponse
                .builder()
                .comparisonId(comparison.getId())
                .componentId(component.getId())
                .componentName(component.getName())
                .componentDescription(
                        component.getDescription()
                )
                .displayOrder(component.getDisplayOrder())
                .disclosureText(
                        comparison.getDisclosureText()
                )
                .citation(comparison.getCitation())
                .comparisonResult(
                        comparison
                                .getComparisonResult()
                                .name()
                )
                .comparisonResultLabel(
                        comparison
                                .getComparisonResult()
                                .getLabel()
                )
                .build();
    }

    private ReportDetailResponse.InventiveStepAnalysisResponse
    toInventiveStepAnalysisResponse(
            InventiveStepAnalysis analysis,
            List<InventiveArgument> arguments
    ) {
        return ReportDetailResponse
                .InventiveStepAnalysisResponse
                .builder()
                .analysisId(analysis.getId())
                .primaryPriorArt(
                        toPriorArtSummary(
                                analysis.getPrimaryArt()
                        )
                )
                .secondaryPriorArt(
                        analysis.getSecondaryArt() == null
                                ? null
                                : toPriorArtSummary(
                                analysis.getSecondaryArt()
                        )
                )
                .arguments(
                        arguments.stream()
                                .map(
                                        this::
                                                toInventiveArgumentResponse
                                )
                                .toList()
                )
                .build();
    }

    private ReportDetailResponse.InventiveArgumentResponse
    toInventiveArgumentResponse(
            InventiveArgument argument
    ) {
        return ReportDetailResponse
                .InventiveArgumentResponse
                .builder()
                .argumentId(argument.getId())
                .argumentType(
                        argument.getArgumentType().name()
                )
                .argumentTypeLabel(
                        argument.getArgumentType().getLabel()
                )
                .recommended(argument.getRecommended())
                .content(argument.getContent())
                .build();
    }

    private ReportDetailResponse.PriorArtSummary
    toPriorArtSummary(
            PriorArt priorArt
    ) {
        return ReportDetailResponse.PriorArtSummary.builder()
                .priorArtId(priorArt.getId())
                .applicationNumber(
                        priorArt.getApplicationNumber()
                )
                .title(priorArt.getTitle())
                .applicantName(priorArt.getApplicantName())
                .applicationDate(
                        priorArt.getApplicationDate()
                )
                .registrationDate(
                        priorArt.getRegistrationDate()
                )
                .legalStatus(priorArt.getLegalStatus())
                .ipcCodes(priorArt.getIpcCodes())
                .build();
    }

    private void validateCaseOwner(
            Case caseEntity,
            Long userId
    ) {
        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.CASE_ACCESS_DENIED
            );
        }
    }

    private void validateAnalysisResults(
            Case caseEntity
    ) {
        noveltyAnalysisRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.NOVELTY_ANALYSIS_NOT_FOUND
                        )
                );

        inventiveStepAnalysisRepository
                .findByCaseEntity(caseEntity)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode
                                        .INVENTIVE_STEP_ANALYSIS_NOT_FOUND
                        )
                );
    }
}