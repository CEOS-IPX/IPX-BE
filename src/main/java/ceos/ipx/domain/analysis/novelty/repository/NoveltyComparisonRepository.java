package ceos.ipx.domain.analysis.novelty.repository;

import ceos.ipx.domain.analysis.novelty.entity.NoveltyAnalysis;
import ceos.ipx.domain.analysis.novelty.entity.NoveltyComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoveltyComparisonRepository extends JpaRepository<NoveltyComparison, Long> {

    /**
     * component와 fetch join으로 N+1 방지
     */
    @Query("""
        SELECT c
        FROM NoveltyComparison c
        JOIN FETCH c.component
        WHERE c.analysis = :analysis
        ORDER BY c.component.displayOrder ASC
        """)
    List<NoveltyComparison> findAllByAnalysis(
            @Param("analysis") NoveltyAnalysis analysis
    );

    @Modifying
    @Query("DELETE FROM NoveltyComparison c " +
            "WHERE c.analysis.id IN (SELECT n.id FROM NoveltyAnalysis n WHERE n.caseEntity.id = :caseId)")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}
