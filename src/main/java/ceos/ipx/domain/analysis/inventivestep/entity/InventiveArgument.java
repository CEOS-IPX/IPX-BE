package ceos.ipx.domain.analysis.inventivestep.entity;

import ceos.ipx.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Table(
        name = "inventive_arguments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ia_analysis_type", columnNames = {"analysis_id", "argument_type"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventiveArgument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private InventiveStepAnalysis analysis;

    @Enumerated(EnumType.STRING)
    @Column(name = "argument_type", nullable = false, length = 30)
    private ArgumentType argumentType;

    /** 적용 여부 (변리사가 선택 시 true) */
    @Column(nullable = false)
    private Boolean applicable;

    /**
     * 논리 유형별 구조화된 데이터
     * - NUMERICAL_LIMIT: {"effect_table": [{"metric": "VOC", "prior": 320, "ours": 8}]}
     * - COMBINATION_MOTIVATION: {"background_limit": "...", "teaching_away": "..."}
     * - COMMON_TECHNIQUE: {"target_component": "B", "rebuttal": "..."}
     * - SIMPLE_DESIGN: {"changed_component": "C", "non_obviousness": "..."}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> content = new HashMap<>();

    @Builder
    private InventiveArgument(InventiveStepAnalysis analysis, ArgumentType argumentType,
                              Boolean applicable, Map<String, Object> content) {
        this.analysis = analysis;
        this.argumentType = argumentType;
        this.applicable = applicable != null ? applicable : false;
        this.content = content != null ? content : new HashMap<>();
    }

    public void markApplicable() {
        this.applicable = true;
    }

    public void markNotApplicable() {
        this.applicable = false;
    }

    public void updateContent(Map<String, Object> content) {
        this.content = content != null ? content : new HashMap<>();
        this.applicable = true;
    }
}