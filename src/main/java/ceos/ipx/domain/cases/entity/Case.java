package ceos.ipx.domain.cases.entity;


import ceos.ipx.domain.user.entity.User;
import ceos.ipx.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "cases", indexes = {
        @Index(name = "idx_cases_user", columnList = "user_id, created_at DESC")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Case extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "applicant_name", length = 200)
    private String applicantName;

    @Column(name = "inventor_name", length = 200)
    private String inventorName;

    @Column(name = "technical_field", columnDefinition = "TEXT")
    private String technicalField;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "user_input_ipc", columnDefinition = "TEXT[]", nullable = false)
    private List<String> userInputIpc = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "TEXT[]", nullable = false)
    private List<String> keywords = new ArrayList<>();

    @Column(name = "search_completed_at")
    private LocalDateTime searchCompletedAt;

    @Column(name = "novelty_completed_at")
    private LocalDateTime noveltyCompletedAt;

    @Column(name = "inventive_completed_at")
    private LocalDateTime inventiveCompletedAt;

    @Column(name = "report_completed_at")
    private LocalDateTime reportCompletedAt;

    @Builder
    private Case(User user, String title, String applicantName, String inventorName,
                 String technicalField, String description, List<String> userInputIpc, List<String> keywords) {
        this.user = user;
        this.title = title;
        this.applicantName = applicantName;
        this.inventorName = inventorName;
        this.technicalField = technicalField;
        this.description = description;
        this.userInputIpc = userInputIpc != null ? userInputIpc : new ArrayList<>();
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    public void updateKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    // ===== 단계 완료 처리 =====
    public void completeSearch() {
        this.searchCompletedAt = LocalDateTime.now();
    }

    public void completeNoveltyAnalysis() {
        this.noveltyCompletedAt = LocalDateTime.now();
    }

    public void completeInventiveStepAnalysis() {
        this.inventiveCompletedAt = LocalDateTime.now();
    }

    public void completeReport() {
        this.reportCompletedAt = LocalDateTime.now();
    }

    // 재검색 시 모든 단계 초기화
    public void resetAllStages() {
        this.searchCompletedAt = null;
        this.noveltyCompletedAt = null;
        this.inventiveCompletedAt = null;
        this.reportCompletedAt = null;
    }

    // ===== 리포트 생성 가능 여부 =====
    public boolean canGenerateReport() {
        return this.noveltyCompletedAt != null && this.inventiveCompletedAt != null;
    }
}