package ceos.ipx.domain.cases.repository;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PriorArtRepository extends JpaRepository<PriorArt, Long> {

    List<PriorArt> findByCaseEntityOrderByRrfScoreDesc(Case caseEntity);

    Optional<PriorArt> findByCaseEntityAndApplicationNumber(Case caseEntity, String applicationNumber);

    @Modifying
    @Query("DELETE FROM PriorArt pa WHERE pa.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}