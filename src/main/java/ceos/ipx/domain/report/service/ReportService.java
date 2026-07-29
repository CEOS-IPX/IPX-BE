package ceos.ipx.domain.report.service;

import ceos.ipx.domain.analysis.inventivestep.entity.ArgumentType;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveArgumentRepository;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveStepAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.entity.ComparisonResult;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyAnalysisRepository;
import ceos.ipx.domain.analysis.novelty.repository.NoveltyComparisonRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * 저장된 신규성·진보성 분석 결과를 바탕으로
     * 분석 리포트를 자동 생성한다.
     */
    @Transactional
    public ReportCreateResponse createReport(
            Long userId,
            Long caseId
    ) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CASE_NOT_FOUND)
                );

        validateCaseOwner(caseEntity, userId);

        if (reportRepository.findByCaseEntity(caseEntity).isPresent()) {
            throw new BusinessException(
                    ErrorCode.REPORT_ALREADY_EXISTS
            );
        }

        NoveltyAnalysis noveltyAnalysis =
                noveltyAnalysisRepository.findByCaseEntity(caseEntity)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.NOVELTY_ANALYSIS_NOT_FOUND
                                )
                        );

        InventiveStepAnalysis inventiveStepAnalysis =
                inventiveStepAnalysisRepository
                        .findByCaseEntity(caseEntity)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVENTIVE_STEP_ANALYSIS_NOT_FOUND
                                )
                        );

        List<NoveltyComparison> comparisons =
                noveltyComparisonRepository
                        .findAllByAnalysis(noveltyAnalysis);

        List<InventiveArgument> arguments =
                inventiveArgumentRepository
                        .findAllByAnalysis(inventiveStepAnalysis);

        boolean noveltySatisfied =
                isNoveltySatisfied(comparisons);

        List<InventiveArgument> recommendedArguments =
                getRecommendedArguments(arguments);

        boolean inventiveSatisfied =
                !recommendedArguments.isEmpty();

        String overallConclusion =
                buildOverallConclusion(
                        noveltyAnalysis,
                        recommendedArguments,
                        noveltySatisfied,
                        inventiveSatisfied
                );

        Report report = Report.builder()
                .caseEntity(caseEntity)
                .authorName(caseEntity.getUser().getName())
                .noveltySatisfied(noveltySatisfied)
                .inventiveSatisfied(inventiveSatisfied)
                .overallConclusion(overallConclusion)
                .build();

        reportRepository.save(report);

        caseEntity.completeReport();

        reportRepository.flush();
        caseRepository.flush();

        return ReportCreateResponse.of(
                report,
                caseEntity
        );
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

        return ReportUpdateResponse.of(
                report,
                caseEntity
        );
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
                        .findAllByAnalysis(inventiveStepAnalysis);

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

    /**
     * 신규성 비교 결과 중 NOVEL 판정이 하나라도 있으면
     * 신규성을 충족한 것으로 판단한다.
     */
    private boolean isNoveltySatisfied(
            List<NoveltyComparison> comparisons
    ) {
        return comparisons.stream()
                .anyMatch(comparison ->
                        comparison.getComparisonResult()
                                == ComparisonResult.NOVEL
                );
    }

    /**
     * 진보성 논리 중 AI가 추천한 항목만 반환한다.
     */
    private List<InventiveArgument> getRecommendedArguments(
            List<InventiveArgument> arguments
    ) {
        return arguments.stream()
                .filter(argument ->
                        Boolean.TRUE.equals(
                                argument.getRecommended()
                        )
                )
                .toList();
    }

    /**
     * 신규성 분석 결론과 추천된 진보성 논리를 조합해
     * 리포트의 종합 결론을 자동 생성한다.
     */
    private String buildOverallConclusion(
            NoveltyAnalysis noveltyAnalysis,
            List<InventiveArgument> recommendedArguments,
            boolean noveltySatisfied,
            boolean inventiveSatisfied
    ) {
        String noveltyConclusion =
                noveltyAnalysis.getConclusionText().trim();

        String inventiveArgumentLabels =
                recommendedArguments.stream()
                        .map(argument ->
                                argument.getArgumentType().getLabel()
                        )
                        .collect(Collectors.joining(", "));

        if (noveltySatisfied && inventiveSatisfied) {
            return String.format(
                    "%s 또한 %s 논리가 도출되어, "
                            + "본 발명은 신규성과 진보성을 모두 충족할 가능성이 "
                            + "있는 것으로 판단됩니다.",
                    noveltyConclusion,
                    inventiveArgumentLabels
            );
        }

        if (noveltySatisfied) {
            return String.format(
                    "%s 다만 적용 가능한 진보성 논리가 확인되지 않아, "
                            + "진보성 충족 여부는 추가 검토가 필요합니다.",
                    noveltyConclusion
            );
        }

        if (inventiveSatisfied) {
            return String.format(
                    "%s %s 논리를 통한 진보성 주장은 가능하나, "
                            + "신규성 부정 가능성에 대한 추가 검토가 필요합니다.",
                    noveltyConclusion,
                    inventiveArgumentLabels
            );
        }

        return String.format(
                "%s 신규성과 진보성 모두 추가 검토가 필요한 것으로 판단됩니다.",
                noveltyConclusion
        );
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
        Map<ArgumentType, InventiveArgument> argumentMap =
                arguments.stream()
                        .collect(
                                Collectors.toMap(
                                        InventiveArgument::getArgumentType,
                                        Function.identity()
                                )
                        );

        List<ReportDetailResponse.InventiveArgumentResponse>
                argumentResponses =
                Arrays.stream(ArgumentType.values())
                        .map(argumentType -> {
                            InventiveArgument argument =
                                    argumentMap.get(argumentType);

                            return argument == null
                                    ? toEmptyInventiveArgumentResponse(
                                    argumentType
                            )
                                    : toInventiveArgumentResponse(
                                    argument
                            );
                        })
                        .toList();

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
                .arguments(argumentResponses)
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

    private ReportDetailResponse.InventiveArgumentResponse
    toEmptyInventiveArgumentResponse(
            ArgumentType argumentType
    ) {
        return ReportDetailResponse
                .InventiveArgumentResponse
                .builder()
                .argumentId(null)
                .argumentType(argumentType.name())
                .argumentTypeLabel(argumentType.getLabel())
                .recommended(false)
                .content(Map.of())
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
}