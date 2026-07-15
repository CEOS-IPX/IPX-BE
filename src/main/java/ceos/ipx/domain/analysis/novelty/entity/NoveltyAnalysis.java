package ceos.ipx.domain.analysis.novelty.entity;

import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.PriorArt;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "d1_prior_art_id", nullable = false)
    private PriorArt d1PriorArt;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_similarity", length = 20, nullable = false)
    private NoveltyVerdict overallSimilarity;

    @Column(name = "conclusion_text", columnDefinition = "TEXT", nullable = false)
    private String conclusionText;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private NoveltyAnalysis(Case caseEntity, PriorArt  d1PriorArt, NoveltyVerdict overallSimilarity, String conclusionText) {
        this.caseEntity = caseEntity;
        this.d1PriorArt =  d1PriorArt;
        this.overallSimilarity = overallSimilarity;
        this.conclusionText = conclusionText;
    }

    public void updateResult(NoveltyVerdict overallSimilarity, String conclusionText) {
        this.overallSimilarity = overallSimilarity;
        this.conclusionText = conclusionText;
    }
}