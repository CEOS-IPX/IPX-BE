package ceos.ipx.domain.cases.repository;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PriorArtRepository extends JpaRepository<PriorArt, Long> {

    List<PriorArt> findByCaseEntityOrderByRrfScoreDescCreatedAtAsc(Case caseEntity);

    long countByCaseEntity(Case caseEntity);

    boolean existsByCaseEntityAndApplicationNumber(
            Case caseEntity,
            String applicationNumber
    );

    @Query("SELECT pa.applicationNumber FROM PriorArt pa " +
            "WHERE pa.caseEntity = :caseEntity " +
            "AND pa.applicationNumber IN :applicationNumbers")
    Set<String> findExistingApplicationNumbers(
            @Param("caseEntity") Case caseEntity,
            @Param("applicationNumbers") List<String> applicationNumbers
    );

    @Modifying
    @Query("DELETE FROM PriorArt pa WHERE pa.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}