package ceos.ipx.domain.cases.entity;

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
@Table(
        name = "invention_components",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ic_case_display_order", columnNames = {"case_id", "display_order"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventionComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private InventionComponent(Case caseEntity, String name, String description, Integer displayOrder) {
        this.caseEntity = caseEntity;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
