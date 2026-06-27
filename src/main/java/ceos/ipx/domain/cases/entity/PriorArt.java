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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "prior_arts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"case_id", "application_number"}),
        indexes = @Index(name = "idx_pa_case_rank", columnList = "case_id, rank"))
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

    /** D1=1, D2=2, D3=3 ... 검색 결과 우선순위 */
    @Column(nullable = false)
    private Short rank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriorArtSource source;

    @Column(name = "rrf_score")
    private Double rrfScore;

    /** 변리사가 분석 대상에서 제외하면 false */
    @Column(nullable = false)
    private Boolean included;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "tech_purpose", columnDefinition = "TEXT")
    private String techPurpose;

    @Column(name = "key_features", columnDefinition = "TEXT")
    private String keyFeatures;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "matched_keywords", columnDefinition = "TEXT[]")
    private List<String> matchedKeywords = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PriorArt(Case caseEntity, String applicationNumber, Short rank,
                     PriorArtSource source, Double rrfScore,
                     String reason, String summary, String techPurpose,
                     String keyFeatures, List<String> matchedKeywords) {
        this.caseEntity = caseEntity;
        this.applicationNumber = applicationNumber;
        this.rank = rank;
        this.source = source;
        this.rrfScore = rrfScore;
        this.included = true;
        this.reason = reason;
        this.summary = summary;
        this.techPurpose = techPurpose;
        this.keyFeatures = keyFeatures;
        this.matchedKeywords = matchedKeywords != null ? matchedKeywords : new ArrayList<>();
    }

    public void exclude() {
        this.included = false;
    }

    public void include() {
        this.included = true;
    }

    /** D1, D2, M1, M2 형태의 라벨 (rank로부터 도출) */
    public String getLabel() {
        return this.source == PriorArtSource.SEARCH ? "D" + this.rank : "M" + this.rank;
    }
}


