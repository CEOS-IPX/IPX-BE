package ceos.ipx.domain.analysis.novelty.entity;

import ceos.ipx.domain.cases.entity.InventionComponent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "novelty_comparisons",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_nc_analysis_component",
                        columnNames = {"analysis_id", "component_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoveltyComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private NoveltyAnalysis analysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private InventionComponent component;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_result", nullable = false, length = 20)
    private ComparisonResult comparisonResult;

    @Column(name = "disclosure_text", columnDefinition = "TEXT", nullable = false)
    private String disclosureText;

    @Column(name = "citation", columnDefinition = "TEXT")
    private String citation;

    @Builder
    private NoveltyComparison(
            NoveltyAnalysis noveltyAnalysis,
            InventionComponent component,
            ComparisonResult comparisonResult,
            String disclosureText,
            String citation
    ) {
        this.analysis = noveltyAnalysis;
        this.component = component;
        this.comparisonResult = comparisonResult;
        this.disclosureText = disclosureText;
        this.citation = citation;
    }

    public void updateComparison(
            ComparisonResult comparisonResult,
            String citation
    ) {
        this.comparisonResult = comparisonResult;
        this.citation = citation;
    }
}