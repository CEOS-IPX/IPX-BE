package ceos.ipx.global.python.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Python /search/add-manual 요청 DTO
 *
 * Python 측 AddManualRequest 스펙과 필드명 일치 (snake_case)
 *
 * Python이 요구하는 필드:
 *   - application_numbers: 추가할 출원번호 리스트 (중복 확인 완료 상태)
 *   - context: 원본 검색의 사용자 발명 정보 (title, description, user_keywords)
 *
 * Spring이 사전에 처리:
 *   - 기존 prior_arts와 중복 필터링
 *   - Case에서 context 조립
 */
public record PythonAddManualRequest(
        @JsonProperty("application_numbers")
        List<String> applicationNumbers,

        SearchContext context
) {

    @Builder
    public record SearchContext(
            String title,
            String description,

            @JsonProperty("user_keywords")
            List<String> userKeywords
    ) {}
}