package ceos.ipx.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ceos.ipx.domain.user.dto.SignUpRequest;
import ceos.ipx.domain.user.dto.SignUpResponse;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.entity.UserProvider;
import ceos.ipx.domain.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ceos.ipx.domain.terms.dto.TermsAgreementRequest;
import ceos.ipx.domain.terms.entity.TermsAgreementType;
import ceos.ipx.domain.terms.service.TermsAgreementService;import ceos.ipx.domain.terms.service.TermsAgreementService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TermsAgreementService termsAgreementService;
    
    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void signUpUsesUserDefaultLocalProvider() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "verification-token",
                "Test User",
                "Password123!",
                "Password123!",
                "IPX",
                List.of(new TermsAgreementRequest(TermsAgreementType.SERVICE_TERMS, true))
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", 1L);
            return savedUser;
        });

        SignUpResponse response = authService.signUp(request);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getProvider()).isEqualTo(UserProvider.LOCAL);
        assertThat(savedUser.getProviderId()).isNull();
        assertThat(savedUser.isActive()).isTrue();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.provider()).isEqualTo(UserProvider.LOCAL.name());
    }
}
