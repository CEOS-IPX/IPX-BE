package ceos.ipx.domain.cases.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "prior_arts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pa_case_app_num",
                        columnNames = {"case_id", "application_number"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_pa_case_rrf",
                        columnList = "case_id, rrf_score DESC"
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriorArt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @Column(name = "application_number", nullable = false, length = 20)
    private String applicationNumber;

    @Column(length = 500)
    private String title;

    @Column(name = "applicant_name", length = 200)
    private String applicantName;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "legal_status", length = 20)
    private String legalStatus;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ipc_codes", columnDefinition = "TEXT[]")
    private List<String> ipcCodes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriorArtSource source;

    @Column(name = "rrf_score", nullable = false)
    private Double rrfScore;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "tech_purpose", columnDefinition = "TEXT")
    private String techPurpose;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "key_features", columnDefinition = "TEXT[]")
    private List<String> keyFeatures = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(
            name = "matched_keywords",
            columnDefinition = "TEXT[]",
            nullable = false
    )
    private List<String> matchedKeywords = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PriorArt(
            Case caseEntity,
            String applicationNumber,
            String title,
            String applicantName,
            LocalDate applicationDate,
            LocalDate registrationDate,
            String legalStatus,
            List<String> ipcCodes,
            PriorArtSource source,
            Double rrfScore,
            String summary,
            String techPurpose,
            List<String> keyFeatures,
            List<String> matchedKeywords,
            String reason
    ) {
        this.caseEntity = caseEntity;
        this.applicationNumber = applicationNumber;
        this.title = title;
        this.applicantName = applicantName;
        this.applicationDate = applicationDate;
        this.registrationDate = registrationDate;
        this.legalStatus = legalStatus;
        this.ipcCodes = ipcCodes != null ? ipcCodes : new ArrayList<>();
        this.source = source;
        this.rrfScore = rrfScore;
        this.summary = summary;
        this.techPurpose = techPurpose;
        this.keyFeatures = keyFeatures != null ? keyFeatures : new ArrayList<>();
        this.matchedKeywords =
                matchedKeywords != null ? matchedKeywords : new ArrayList<>();
        this.reason = reason;
    }
}