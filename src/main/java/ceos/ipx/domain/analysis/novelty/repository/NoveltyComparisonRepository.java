package ceos.ipx.domain.analysis.novelty.repository;

import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoveltyComparisonRepository extends JpaRepository<NoveltyComparison, Long> {

    @Modifying
    @Query("DELETE FROM NoveltyComparison c " +
            "WHERE c.analysis.id IN (SELECT n.id FROM NoveltyAnalysis n WHERE n.caseEntity.id = :caseId)")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}
