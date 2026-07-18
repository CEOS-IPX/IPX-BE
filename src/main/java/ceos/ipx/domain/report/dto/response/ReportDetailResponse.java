package ceos.ipx.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@Schema(description = "분석 리포트 상세 조회 응답")
public class ReportDetailResponse {

    @Schema(description = "리포트 ID", example = "1")
    private Long reportId;

    @Schema(description = "사건 ID", example = "1")
    private Long caseId;

    @Schema(
            description = "사건 제목",
            example = "고효율 리튬 이차전지용 전극 구조"
    )
    private String caseTitle;

    @Schema(description = "출원인명", example = "주식회사 아이피엑스")
    private String applicantName;

    @Schema(description = "발명자명", example = "홍길동")
    private String inventorName;

    @Schema(
            description = "기술 분야",
            example = "본 발명은 리튬 이차전지용 전극 구조에 관한 것이다."
    )
    private String technicalField;

    @Schema(
            description = "발명 설명",
            example = "전극의 구조를 개선하여 충전 효율과 수명을 향상시키는 기술이다."
    )
    private String description;

    @Schema(description = "작성 변리사명", example = "오지송")
    private String authorName;

    @Schema(description = "신규성 최종 충족 여부", example = "true")
    private Boolean noveltySatisfied;

    @Schema(description = "진보성 최종 충족 여부", example = "true")
    private Boolean inventiveSatisfied;

    @Schema(
            description = "분석 리포트 종합 결론",
            example = "본 발명은 신규성과 진보성을 모두 충족하는 것으로 판단됩니다."
    )
    private String overallConclusion;

    @Schema(description = "발명 구성요소 목록")
    private List<ComponentResponse> components;

    @Schema(description = "신규성 분석 결과")
    private NoveltyAnalysisResponse noveltyAnalysis;

    @Schema(description = "진보성 분석 결과")
    private InventiveStepAnalysisResponse inventiveStepAnalysis;

    @Schema(
            description = "사건의 리포트 생성 완료 시각",
            example = "2026-07-18T14:30:00"
    )
    private LocalDateTime reportCompletedAt;

    @Schema(
            description = "리포트 최초 생성일시",
            example = "2026-07-18T14:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "리포트 최종 수정일시",
            example = "2026-07-18T15:10:00"
    )
    private LocalDateTime updatedAt;


    @Getter
    @Builder
    @Schema(description = "발명 구성요소")
    public static class ComponentResponse {

        @Schema(description = "구성요소 ID", example = "1")
        private Long componentId;

        @Schema(description = "구성요소명", example = "전극 활물질층")
        private String name;

        @Schema(
                description = "구성요소 설명",
                example = "집전체의 적어도 일면에 형성되는 전극 활물질층"
        )
        private String description;

        @Schema(description = "화면 표시 순서", example = "1")
        private Short displayOrder;
    }


    @Getter
    @Builder
    @Schema(description = "신규성 분석 결과")
    public static class NoveltyAnalysisResponse {

        @Schema(description = "신규성 분석 ID", example = "1")
        private Long analysisId;

        @Schema(
                description = "전체 유사도 판단 enum 값",
                example = "HIGH"
        )
        private String overallSimilarity;

        @Schema(
                description = "전체 유사도 판단 한글 라벨",
                example = "높음"
        )
        private String overallSimilarityLabel;

        @Schema(
                description = "신규성 분석 결론",
                example = "일부 구성요소가 선행문헌에 개시되어 있으나 차별되는 구성이 존재합니다."
        )
        private String conclusionText;

        @Schema(description = "신규성 분석의 주인용 선행문헌")
        private PriorArtSummary primaryPriorArt;

        @Schema(description = "구성요소별 신규성 비교 결과")
        private List<NoveltyComparisonResponse> comparisons;
    }


    @Getter
    @Builder
    @Schema(description = "구성요소별 신규성 비교 결과")
    public static class NoveltyComparisonResponse {

        @Schema(description = "신규성 비교 결과 ID", example = "1")
        private Long comparisonId;

        @Schema(description = "구성요소 ID", example = "1")
        private Long componentId;

        @Schema(description = "구성요소명", example = "전극 활물질층")
        private String componentName;

        @Schema(
                description = "구성요소 설명",
                example = "집전체의 적어도 일면에 형성되는 전극 활물질층"
        )
        private String componentDescription;

        @Schema(description = "구성요소 화면 표시 순서", example = "1")
        private Short displayOrder;

        @Schema(
                description = "선행문헌 개시 내용",
                example = "선행문헌에는 집전체 표면에 활물질층을 형성하는 구성이 개시되어 있습니다."
        )
        private String disclosureText;

        @Schema(
                description = "선행문헌 원문 인용 부분",
                example = "청구항 1 및 문단 [0032]",
                nullable = true
        )
        private String citation;

        @Schema(
                description = "비교 결과 enum 값",
                example = "SIMILAR"
        )
        private String comparisonResult;

        @Schema(
                description = "비교 결과 한글 라벨",
                example = "유사"
        )
        private String comparisonResultLabel;
    }


    @Getter
    @Builder
    @Schema(description = "진보성 분석 결과")
    public static class InventiveStepAnalysisResponse {

        @Schema(description = "진보성 분석 ID", example = "1")
        private Long analysisId;

        @Schema(description = "주인용 선행문헌 D1")
        private PriorArtSummary primaryPriorArt;

        @Schema(
                description = "부인용 선행문헌 D2",
                nullable = true
        )
        private PriorArtSummary secondaryPriorArt;

        @Schema(description = "진보성 판단 논리 목록")
        private List<InventiveArgumentResponse> arguments;
    }


    @Getter
    @Builder
    @Schema(description = "진보성 판단 논리")
    public static class InventiveArgumentResponse {

        @Schema(description = "진보성 논리 ID", example = "1")
        private Long argumentId;

        @Schema(
                description = "진보성 논리 유형 enum 값",
                example = "NUMERICAL_LIMIT"
        )
        private String argumentType;

        @Schema(
                description = "진보성 논리 유형 한글 라벨",
                example = "수치한정"
        )
        private String argumentTypeLabel;

        @Schema(description = "AI 추천 여부", example = "true")
        private Boolean recommended;

        @Schema(
                description = "논리 유형별 구조화된 JSON 데이터",
                example = """
                        {
                          "effect_table": [
                            {
                              "metric": "VOC",
                              "prior": 320,
                              "ours": 8
                            }
                          ]
                        }
                        """
        )
        private Map<String, Object> content;
    }


    @Getter
    @Builder
    @Schema(description = "선행문헌 요약 정보")
    public static class PriorArtSummary {

        @Schema(description = "선행문헌 ID", example = "1")
        private Long priorArtId;

        @Schema(
                description = "출원번호",
                example = "10-2024-0012345"
        )
        private String applicationNumber;

        @Schema(
                description = "발명의 명칭",
                example = "리튬 이차전지용 전극 및 그 제조방법"
        )
        private String title;

        @Schema(description = "출원인명", example = "주식회사 아이피엑스")
        private String applicantName;

        @Schema(
                description = "출원일",
                example = "2024-01-15",
                nullable = true
        )
        private LocalDate applicationDate;

        @Schema(
                description = "등록일",
                example = "2025-06-20",
                nullable = true
        )
        private LocalDate registrationDate;

        @Schema(
                description = "법적 상태",
                example = "등록",
                nullable = true
        )
        private String legalStatus;

        @Schema(
                description = "IPC 코드 목록",
                example = "[\"H01M 4/13\", \"H01M 10/0525\"]"
        )
        private List<String> ipcCodes;
    }
}