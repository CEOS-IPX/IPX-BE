package ceos.ipx.domain.analysis.inventivestep.repository;

import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveStepAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventiveArgumentRepository extends JpaRepository<InventiveArgument, Long> {

    List<InventiveArgument> findAllByAnalysis(InventiveStepAnalysis analysis);

    @Modifying
    @Query("DELETE FROM InventiveArgument a " +
            "WHERE a.analysis.id IN (SELECT i.id FROM InventiveStepAnalysis i WHERE i.caseEntity.id = :caseId)")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}