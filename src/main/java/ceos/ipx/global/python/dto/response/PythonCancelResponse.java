package ceos.ipx.global.python.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python /search/{search_id}/cancel 응답.
 *
 * Python 측 CancelResponse:
 *   - search_id: 취소된 검색 ID
 *   - cancelled: 실제로 취소가 발생했는지 (이미 완료 상태였으면 false)
 */
public record PythonCancelResponse(
        @JsonProperty("search_id")
        String searchId,

        Boolean cancelled
) {}