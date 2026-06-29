package ceos.ipx.domain.analysis.inventivestep.entity;

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
@Table(name = "inventive_step_analyses")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventiveStepAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private Case caseEntity;

    /** D1 - 사용자가 선택한 주인용 발명 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_art_id", nullable = false)
    private PriorArt primaryArt;

    /** D2 - AI가 추천한 부인용 발명 (선택) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_art_id")
    private PriorArt secondaryArt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private InventiveStepAnalysis(Case caseEntity, PriorArt primaryArt, PriorArt secondaryArt) {
        this.caseEntity = caseEntity;
        this.primaryArt = primaryArt;
        this.secondaryArt = secondaryArt;
    }

    public void selectSecondary(PriorArt secondaryArt) {
        this.secondaryArt = secondaryArt;
    }
}