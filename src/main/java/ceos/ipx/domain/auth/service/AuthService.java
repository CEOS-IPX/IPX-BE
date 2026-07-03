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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;
    private final CookieUtils cookieUtils;

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