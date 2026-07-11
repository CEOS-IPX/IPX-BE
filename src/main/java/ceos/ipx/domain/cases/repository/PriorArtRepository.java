package ceos.ipx.domain.cases.repository;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PriorArtRepository extends JpaRepository<PriorArt, Long> {

    /**
     * 사건별 결과 조회
     * 정렬: rrf_score DESC → created_at ASC
     *
     * created_at 정렬:
     *   - 수동 추가된 특허들은 모두 같은 forced_rrf 값을 가짐
     *   - 같은 rrf_score 내에서 추가 순서대로 표시하기 위함
     */
    List<PriorArt> findByCaseEntityOrderByRrfScoreDescCreatedAtAsc(Case caseEntity);

    /**
     * 특정 특허 존재 확인
     */
    boolean existsByCaseEntityAndApplicationNumber(Case caseEntity, String applicationNumber);

    /**
     * 특정 사건의 여러 출원번호 중 이미 존재하는 것들 조회
     * 수동 추가 시 중복 필터링에 사용
     */
    @Query("SELECT pa.applicationNumber FROM PriorArt pa " +
            "WHERE pa.caseEntity = :caseEntity " +
            "AND pa.applicationNumber IN :applicationNumbers")
    Set<String> findExistingApplicationNumbers(
            @Param("caseEntity") Case caseEntity,
            @Param("applicationNumbers") List<String> applicationNumbers
    );

    /**
     * 재검색 시 기존 결과 삭제
     */
    @Modifying
    @Query("DELETE FROM PriorArt pa WHERE pa.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}