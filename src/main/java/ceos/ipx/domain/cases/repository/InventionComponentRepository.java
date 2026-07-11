package ceos.ipx.domain.cases.repository;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventionComponentRepository extends JpaRepository<InventionComponent, Long> {

    List<InventionComponent> findByCaseEntityOrderByDisplayOrderAsc(Case caseEntity);

    @Modifying
    @Query("DELETE FROM InventionComponent ic WHERE ic.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}
