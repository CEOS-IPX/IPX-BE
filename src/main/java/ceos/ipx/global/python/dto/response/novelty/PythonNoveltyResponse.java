package ceos.ipx.global.python.dto.response.novelty;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Python /analyze/novelty 응답
 *
 * Python이 3건 병렬 분석 후 가장 유사한 1건(D1) 정보를 반환
 *
 * - d1_application_number: 선정된 D1의 출원번호
 * - overall_similarity: "매우 높음" | "높음" | "보통" | "낮음" | "매우 낮음"
 *                       (Spring에서 NoveltyVerdict enum으로 매핑)
 * - conclusion_text: 신규성 판단 결론
 * - component_results: 구성요소별 대비 결과
 */
public record PythonNoveltyResponse(

        @JsonProperty("d1_application_number")
        String d1ApplicationNumber,

        @JsonProperty("overall_similarity")
        String overallSimilarity,

        @JsonProperty("conclusion_text")
        String conclusionText,

        @JsonProperty("component_results")
        List<PythonComponentComparison> componentResults
) {}