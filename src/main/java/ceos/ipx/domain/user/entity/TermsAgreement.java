package ceos.ipx.domain.user.entity;

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
@Table(name = "terms_agreements", indexes = {
        @Index(name = "idx_ta_user", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 50)
    private TermsType termsType;

    @Column(name = "terms_version", nullable = false, length = 20)
    private String termsVersion;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private TermsAgreement(User user, TermsType termsType, String termsVersion) {
        this.user = user;
        this.termsType = termsType;
        this.termsVersion = termsVersion;
    }
}