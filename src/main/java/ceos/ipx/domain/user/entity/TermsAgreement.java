package ceos.ipx.domain.user.entity;

import ceos.ipx.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "terms_agreements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ta_user_type_version", columnNames = {"user_id", "type", "terms_version"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermsType type;

    @Column(nullable = false)
    private Boolean agreed;

    @Column(name = "terms_version", nullable = false, length = 20)
    private String termsVersion;

    @Builder
    private TermsAgreement(User user, TermsType type, Boolean agreed, String termsVersion) {
        this.user = user;
        this.type = type;
        this.agreed = agreed;
        this.termsVersion = termsVersion;
    }
}