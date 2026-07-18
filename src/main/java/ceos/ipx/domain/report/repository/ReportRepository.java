package ceos.ipx.domain.report.repository;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByCaseEntity(Case caseEntity);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.caseEntity.id = :caseId")
    void deleteAllByCaseId(@Param("caseId") Long caseId);
}