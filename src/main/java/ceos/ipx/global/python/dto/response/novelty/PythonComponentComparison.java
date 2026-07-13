package ceos.ipx.global.python.dto.response.novelty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 구성요소 1개에 대한 대비 결과
 *
 * Python 측 ComponentComparison과 필드명 일치:
 *   - component_label: 구성요소 라벨 (A, B, C, ...)
 *   - disclosure_text: 선행기술의 대응 개시 내용
 *   - citation: 원문 인용 (Optional)
 *   - result: "동일" | "유사" | "신규" (Spring에서 ComparisonResult enum으로 매핑)
 */
public record PythonComponentComparison(

        @JsonProperty("component_label")
        String componentLabel,

        @JsonProperty("disclosure_text")
        String disclosureText,

        String citation,

        String result
) {}
