package ceos.ipx.domain.analysis.novelty.repository;

import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.cases.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoveltyAnalysisRepository extends JpaRepository<NoveltyAnalysis, Long> {

    /**
     * Case로 저장된 분석 조회 (D1 fetch join)
     * GET 조회 시 응답 조립에서 PriorArt 접근이 필요하므로
     */
    @Query("SELECT n FROM NoveltyAnalysis n " +
            "JOIN FETCH n.d1PriorArt " +
            "WHERE n.caseEntity = :caseEntity")
    Optional<NoveltyAnalysis> findByCaseEntity(Case caseEntity);

    boolean existsByD1PriorArt_Id(Long priorArtId);

    @Modifying
    @Query("DELETE FROM NoveltyAnalysis n WHERE n.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}