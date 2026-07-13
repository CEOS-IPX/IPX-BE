package ceos.ipx.global.python.dto.response.inventivestep;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Python /analyze/inventive-step/generate/numerical-limit
 *
 * effect_items: 발명의 효과 표 (수치 비교)
 * 각 EffectItem은 특정 지표에서 D1 대비 본 발명의 개선을 정량적으로 표시
 *
 * Python이 명시된 수치가 없다고 판단하면 빈 배열 반환
 */
public record PythonNumericalLimitResult(

        @JsonProperty("effect_items")
        List<EffectItem> effectItems
) {

    public record EffectItem(

            /** 측정 지표 (예: "VOC 배출량") */
            String metric,

            /** 단위 (예: "g/L", "%") */
            String unit,

            /** 종래기술 수치 */
            @JsonProperty("prior_art_value")
            String priorArtValue,

            /** 본 발명 수치 */
            @JsonProperty("invention_value")
            String inventionValue,

            /** 개선률 (예: "97.5%") */
            String improvement
    ) {}
}