package ceos.ipx.domain.analysis.inventivestep.repository;

import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventiveStepAnalysisRepository extends JpaRepository<InventiveStepAnalysis, Long> {

    @Modifying
    @Query("DELETE FROM InventiveStepAnalysis i WHERE i.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}