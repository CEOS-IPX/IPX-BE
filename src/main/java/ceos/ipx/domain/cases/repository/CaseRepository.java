package ceos.ipx.domain.cases.repository;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<Case, Long> {

    Optional<Case> findByIdAndUser(Long id, User user);

    List<Case> findByUserOrderByCreatedAtDesc(User user);
}
