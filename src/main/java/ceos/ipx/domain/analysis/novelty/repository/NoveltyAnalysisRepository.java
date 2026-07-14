package ceos.ipx.domain.analysis.novelty.repository;

import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.cases.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoveltyAnalysisRepository extends JpaRepository<NoveltyAnalysis, Long> {

    Optional<NoveltyAnalysis> findByCaseEntity(Case caseEntity);

    @Modifying
    @Query("DELETE FROM NoveltyAnalysis n WHERE n.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}