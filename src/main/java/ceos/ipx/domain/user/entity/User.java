package ceos.ipx.domain.user.entity;

import ceos.ipx.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_provider", columnNames = {"provider", "provider_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserProvider provider = UserProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    private User(
            String email,
            String passwordHash,
            String name,
            String company,
            UserProvider provider,
            String providerId,
            Boolean isActive
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.company = company;
        this.provider = provider == null ? UserProvider.LOCAL : provider;
        this.providerId = providerId;
        this.isActive = isActive == null ? true : isActive;
    }

    public void updateProfile(String name, String company) {
        this.name = name;
        this.company = company;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updatePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }
}
