package ceos.ipx.global.python.dto.response.search;

import java.util.List;

/**
 * Python /search/add-manual 응답 DTO
 *
 * Python 측 AddManualResponse:
 *   - results: 새로 추가된 특허 정보 리스트
 */
public record PythonAddManualResponse(
        List<PythonSearchResultResponse.PatentResult> results
) {}