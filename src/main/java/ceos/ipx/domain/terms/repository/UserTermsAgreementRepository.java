package ceos.ipx.domain.terms.repository;

import ceos.ipx.domain.terms.entity.UserTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {
}