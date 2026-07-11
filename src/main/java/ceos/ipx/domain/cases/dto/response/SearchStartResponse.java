package ceos.ipx.domain.cases.dto.response;

/**
 * 검색 시작 즉시 응답 (Spring → 프론트)
 *
 * 검색 실행은 백그라운드에서 진행 (@Async)
 * 프론트는 이 응답의 searchId로 폴링해서 진행 상태 조회
 * 완료되면 caseId로 GET /prior-arts 호출
 */
public record SearchStartResponse(
        String searchId,
        Long caseId,
        String status
) {
    public static SearchStartResponse of(String searchId, Long caseId) {
        return new SearchStartResponse(searchId, caseId, "started");
    }
}
