package ceos.ipx.domain.cases.repository;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.repository.projection.CaseListProjection;
import ceos.ipx.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<Case, Long> {

    Optional<Case> findByIdAndUser(Long id, User user);

    List<Case> findByUserOrderByCreatedAtDesc(User user);

    @Query(
            value = """
                    SELECT c AS caseEntity,
                           COUNT(pa.id) AS priorArtCount
                    FROM Case c
                    LEFT JOIN PriorArt pa ON pa.caseEntity = c
                    WHERE c.user.id = :userId
                      AND (
                          :completed IS NULL
                          OR (:completed = true AND c.reportCompletedAt IS NOT NULL)
                          OR (:completed = false AND c.reportCompletedAt IS NULL)
                      )
                      AND (
                          :keyword IS NULL
                          OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(c.applicantName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(c.inventorName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(c.technicalField) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                    GROUP BY c
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM Case c
                    WHERE c.user.id = :userId
                      AND (
                          :completed IS NULL
                          OR (:completed = true AND c.reportCompletedAt IS NOT NULL)
                          OR (:completed = false AND c.reportCompletedAt IS NULL)
                      )
                      AND (
                          :keyword IS NULL
                          OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(c.applicantName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(c.inventorName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(c.technicalField) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                    """
    )
    Page<CaseListProjection> findCaseList(
            @Param("userId") Long userId,
            @Param("completed") Boolean completed,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Case> findByUserIdOrderByUpdatedAtDescIdDesc(
            Long userId,
            Pageable pageable
    );

    long countByUserId(Long userId);

    long countByUserIdAndReportCompletedAtIsNull(Long userId);

    long countByUserIdAndReportCompletedAtIsNotNull(Long userId);
}