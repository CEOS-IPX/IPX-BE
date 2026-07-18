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

    /**
     * Case로 분석 + D1/D2 fetch join 조회
     * GET 조회 시 응답 조립에서 PriorArt 접근이 필요하므로
     */
    @Query("SELECT a FROM InventiveStepAnalysis a " +
            "JOIN FETCH a.primaryArt " +
            "LEFT JOIN FETCH a.secondaryArt " +
            "WHERE a.caseEntity = :caseEntity")
    Optional<InventiveStepAnalysis> findByCaseEntityWithArts(@Param("caseEntity") Case caseEntity);

    boolean existsByPrimaryArt_IdOrSecondaryArt_Id(
            Long primaryArtId,
            Long secondaryArtId
    );

    @Modifying
    @Query("DELETE FROM InventiveStepAnalysis i WHERE i.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}