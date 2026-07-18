package ceos.ipx.global.python.dto.request.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Python 서버에 전달할 검색 요청 DTO
 *
 * Python 측 SearchRequest 스펙과 필드명 일치 (snake_case)
 *
 * Python이 요구하는 필드:
 *   - case_id (필수): Spring이 생성한 사건 ID
 *   - title (필수): 발명의 명칭
 *   - description (필수): 발명의 핵심 기술 설명
 *   - technical_field (선택): 기술 분야
 *   - user_input_ipc (선택): 변리사가 직접 입력한 IPC (없으면 Python이 추정)
 *   - result_count (선택, 기본 10, 1-30): 결과 개수
 *   - required_application_numbers (선택): 반드시 포함할 출원번호
 *
 * @JsonInclude(NON_NULL)로 null 필드는 JSON에서 제외
 * → Python 측 Optional 필드에 default 값이 정상 적용되도록 함
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonSearchRequest(
        @JsonProperty("case_id")
        String caseId,

        String title,

        String description,

        @JsonProperty("technical_field")
        String technicalField,

        @JsonProperty("user_input_ipc")
        List<String> userInputIpc,

        @JsonProperty("result_count")
        Integer resultCount,

        @JsonProperty("required_application_numbers")
        List<String> requiredApplicationNumbers
) {}
