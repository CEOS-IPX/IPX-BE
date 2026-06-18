package ceos.ipx.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void builderDefaultsProviderToLocalWhenOmitted() {
        User user = User.builder()
                .email("local@example.com")
                .passwordHash("encoded-password")
                .name("Local User")
                .company("IPX")
                .providerId(null)
                .isActive(true)
                .build();

        assertThat(user.getProvider()).isEqualTo(UserProvider.LOCAL);
    }

    @Test
    void builderKeepsExplicitGoogleProvider() {
        User user = User.builder()
                .email("google@example.com")
                .passwordHash("encoded-password")
                .name("Google User")
                .company("IPX")
                .provider(UserProvider.GOOGLE)
                .providerId("google-provider-id")
                .isActive(true)
                .build();

        assertThat(user.getProvider()).isEqualTo(UserProvider.GOOGLE);
    }
}
