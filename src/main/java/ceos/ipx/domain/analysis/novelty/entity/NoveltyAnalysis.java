package ceos.ipx.domain.analysis.novelty.entity;

import ceos.ipx.domain.cases.entity.Case;
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
@Table(name = "novelty_analyses")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoveltyAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private Case caseEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_verdict", length = 20)
    private NoveltyVerdict overallVerdict;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private NoveltyAnalysis(Case caseEntity, NoveltyVerdict overallVerdict, String summary) {
        this.caseEntity = caseEntity;
        this.overallVerdict = overallVerdict;
        this.summary = summary;
    }

    public void updateResult(NoveltyVerdict verdict, String summary) {
        this.overallVerdict = verdict;
        this.summary = summary;
    }
}