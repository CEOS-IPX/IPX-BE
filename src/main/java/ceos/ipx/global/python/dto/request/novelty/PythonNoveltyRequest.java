package ceos.ipx.global.python.dto.request.novelty;

import ceos.ipx.global.python.dto.common.PythonInventionComponent;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Python /analyze/novelty
 *
 * Spring이 사건 정보, 구성요소, 상위 3건 선행기술을 전달
 * Python은 3건에 대해 병렬 LLM 분석 후 가장 유사한 1건을 D1으로 반환
 */
@Builder
public record PythonNoveltyRequest(

        @JsonProperty("invention_title")
        String inventionTitle,

        @JsonProperty("invention_description")
        String inventionDescription,

        List<PythonInventionComponent> components,

        @JsonProperty("prior_arts")
        List<PythonPriorArtForAnalysis> priorArts
) {}