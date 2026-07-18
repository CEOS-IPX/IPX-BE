package ceos.ipx.domain.report.entity;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private Case caseEntity;

    /** 작성 변리사 이름 */
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(name = "novelty_satisfied", nullable = false)
    private Boolean noveltySatisfied;

    @Column(name = "inventive_satisfied", nullable = false)
    private Boolean inventiveSatisfied;

    @Column(name = "overall_conclusion", nullable = false, columnDefinition = "TEXT")
    private String overallConclusion;

    @Builder
    private Report(
            Case caseEntity,
            String authorName,
            Boolean noveltySatisfied,
            Boolean inventiveSatisfied,
            String overallConclusion
    ) {
        this.caseEntity = caseEntity;
        this.authorName = authorName;
        this.noveltySatisfied = noveltySatisfied;
        this.inventiveSatisfied = inventiveSatisfied;
        this.overallConclusion = overallConclusion;
    }

    public void update(
            String authorName,
            Boolean noveltySatisfied,
            Boolean inventiveSatisfied,
            String overallConclusion
    ) {
        this.authorName = authorName;
        this.noveltySatisfied = noveltySatisfied;
        this.inventiveSatisfied = inventiveSatisfied;
        this.overallConclusion = overallConclusion;
    }
}