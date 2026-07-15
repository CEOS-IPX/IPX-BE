package ceos.ipx.domain.user.service;

import ceos.ipx.domain.user.dto.MyInfoResponse;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.domain.user.repository.UserRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MyInfoResponse getMyInfo(String authorizationHeader) {
        String accessToken = extractAccessToken(authorizationHeader);

        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MyInfoResponse.from(user);
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