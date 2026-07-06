package ceos.ipx.domain.auth.service;

import ceos.ipx.domain.auth.dto.LoginRequest;
import ceos.ipx.domain.auth.dto.LoginResponse;
import ceos.ipx.domain.auth.dto.LoginUserResponse;
import ceos.ipx.domain.auth.dto.ReissueResponse;
import ceos.ipx.domain.user.dto.SignUpRequest;
import ceos.ipx.domain.user.dto.SignUpResponse;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.security.cookie.CookieUtils;
import ceos.ipx.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ceos.ipx.domain.auth.dto.PasswordResetRequest;
import ceos.ipx.domain.user.entity.UserProvider;
import java.util.regex.Pattern;
import ceos.ipx.domain.auth.dto.GoogleOAuthTokenRequest;
import ceos.ipx.domain.auth.dto.GoogleTokenResponse;
import ceos.ipx.domain.auth.dto.GoogleUserInfoResponse;
import ceos.ipx.domain.auth.dto.OAuthSignupRequiredResponse;
import ceos.ipx.domain.auth.dto.OAuthTokenResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;
    private final CookieUtils cookieUtils;
    private final EmailVerificationService emailVerificationService;
    private final GoogleOAuthClient googleOAuthClient;
    private final OAuthSignupTokenService oauthSignupTokenService;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!request.password().equals(request.passwordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // TODO: 이메일 인증 토큰 검증 로직 추가
        // TODO: 필수 약관 동의 검증 및 저장 로직 추가

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .passwordHash(encodedPassword)
                .name(request.name())
                .company(request.company())
                .providerId(null)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return new SignUpResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getCompany(),
                savedUser.getProvider().name(),
                true
        );
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse httpServletResponse) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        refreshTokenService.saveRefreshToken(
                user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        cookieUtils.addRefreshTokenCookie(
                httpServletResponse,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        return new LoginResponse(
                accessToken,
                jwtTokenProvider.getTokenType(),
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                LoginUserResponse.from(user)
        );
    }

    @Transactional
    public OAuthTokenResponse exchangeGoogleOAuthToken(
            GoogleOAuthTokenRequest request,
            HttpServletResponse httpServletResponse
    ) {
        GoogleTokenResponse googleTokenResponse = googleOAuthClient.exchangeCodeForToken(request.code());
        GoogleUserInfoResponse googleUserInfo = googleOAuthClient.getUserInfo(googleTokenResponse.accessToken());

        return userRepository.findByEmail(googleUserInfo.email())
                .map(user -> handleExistingGoogleOAuthUser(user, httpServletResponse))
                .orElseGet(() -> handleNewGoogleOAuthUser(googleUserInfo));
    }

    public ReissueResponse reissue(String refreshToken, HttpServletResponse httpServletResponse) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId;
        try {
            userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!refreshTokenService.matches(userId, refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        refreshTokenService.saveRefreshToken(
                user.getId(),
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        cookieUtils.addRefreshTokenCookie(
                httpServletResponse,
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        return ReissueResponse.builder()
                .accessToken(newAccessToken)
                .tokenType(jwtTokenProvider.getTokenType())
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .build();
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String email = emailVerificationService.getEmailByPasswordResetToken(request.getVerificationToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validatePasswordResetUser(user);
        validateNewPassword(request.getNewPassword(), request.getNewPasswordConfirm(), user);

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedPassword);

        emailVerificationService.deletePasswordResetVerification(request.getVerificationToken(), email);
    }

    private OAuthTokenResponse handleExistingGoogleOAuthUser(
            User user,
            HttpServletResponse httpServletResponse
    ) {
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        if (user.getProvider() == UserProvider.LOCAL) {
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_NOT_ALLOWED);
        }

        if (user.getProvider() != UserProvider.GOOGLE) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        refreshTokenService.saveRefreshToken(
                user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        cookieUtils.addRefreshTokenCookie(
                httpServletResponse,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        LoginResponse loginResponse = new LoginResponse(
                accessToken,
                jwtTokenProvider.getTokenType(),
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                LoginUserResponse.from(user)
        );

        return OAuthTokenResponse.loginSuccess(loginResponse);
    }

    private void validatePasswordResetUser(User user) {
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        if (user.getProvider() != UserProvider.LOCAL) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED);
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED);
        }
    }

    private void validateNewPassword(String newPassword, String newPasswordConfirm, User user) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }
    }

    private OAuthTokenResponse handleNewGoogleOAuthUser(GoogleUserInfoResponse googleUserInfo) {
        String oauthSignupToken = oauthSignupTokenService.saveGoogleUserInfo(googleUserInfo);

        OAuthSignupRequiredResponse signupResponse = new OAuthSignupRequiredResponse(
                oauthSignupToken,
                googleUserInfo.email(),
                googleUserInfo.name(),
                UserProvider.GOOGLE.name()
        );

        return OAuthTokenResponse.needSignup(signupResponse);
    }

    @Transactional
    public void logout(
            String authorizationHeader,
            String refreshToken,
            HttpServletResponse httpServletResponse
    ) {
        String accessToken = extractAccessToken(authorizationHeader);

        if (accessTokenBlacklistService.isBlacklisted(accessToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        Long userId;
        try {
            userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        refreshTokenService.deleteRefreshToken(userId);

        long remainingExpirationMillis = jwtTokenProvider.getRemainingExpirationMillis(accessToken);
        accessTokenBlacklistService.blacklist(accessToken, remainingExpirationMillis);

        cookieUtils.deleteRefreshTokenCookie(httpServletResponse);
    }

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());

        if (accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        return accessToken;
    }
}