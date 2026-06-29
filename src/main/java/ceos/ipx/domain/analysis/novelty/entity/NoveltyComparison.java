package ceos.ipx.domain.analysis.novelty.entity;

import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.entity.PriorArt;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "novelty_comparisons",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "novelty_analysis_id", "component_id", "prior_art_id"
        }),
        indexes = @Index(name = "idx_nc_analysis", columnList = "novelty_analysis_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoveltyComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novelty_analysis_id", nullable = false)
    private NoveltyAnalysis noveltyAnalysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private InventionComponent component;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prior_art_id", nullable = false)
    private PriorArt priorArt;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 20)
    private MatchStatus matchStatus;

    @Column(name = "prior_art_excerpt", columnDefinition = "TEXT")
    private String priorArtExcerpt;

    @Builder
    private NoveltyComparison(NoveltyAnalysis noveltyAnalysis,
                              InventionComponent component,
                              PriorArt priorArt,
                              MatchStatus matchStatus,
                              String priorArtExcerpt) {
        this.noveltyAnalysis = noveltyAnalysis;
        this.component = component;
        this.priorArt = priorArt;
        this.matchStatus = matchStatus;
        this.priorArtExcerpt = priorArtExcerpt;
    }
}