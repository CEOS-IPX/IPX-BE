package ceos.ipx.domain.terms.entity;

import ceos.ipx.domain.user.entity.User;
import ceos.ipx.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "terms_agreements",
        indexes = {
                @Index(name = "idx_terms_agreements_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_terms_agreements_user_type_version",
                        columnNames = {"user_id", "type", "terms_version"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermsAgreementType type;

    @Column(nullable = false)
    private boolean agreed;

    @Column(name = "terms_version", nullable = false, length = 20)
    private String termsVersion;

    @Builder
    private UserTermsAgreement(
            User user,
            TermsAgreementType type,
            boolean agreed,
            String termsVersion
    ) {
        this.user = user;
        this.type = type;
        this.agreed = agreed;
        this.termsVersion = termsVersion;
    }
}