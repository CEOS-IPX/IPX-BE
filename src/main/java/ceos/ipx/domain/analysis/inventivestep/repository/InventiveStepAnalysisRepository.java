package ceos.ipx.domain.analysis.inventivestep.repository;

import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import ceos.ipx.domain.cases.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventiveStepAnalysisRepository extends JpaRepository<InventiveStepAnalysis, Long> {

    Optional<InventiveStepAnalysis> findByCaseEntity(Case caseEntity);

    @Modifying
    @Query("DELETE FROM InventiveStepAnalysis i WHERE i.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}