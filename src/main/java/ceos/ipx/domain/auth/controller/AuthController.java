package ceos.ipx.domain.auth.controller;

import ceos.ipx.domain.auth.dto.LoginRequest;
import ceos.ipx.domain.auth.dto.LoginResponse;
import ceos.ipx.domain.auth.service.AuthService;
import ceos.ipx.domain.user.dto.SignUpRequest;
import ceos.ipx.domain.user.dto.SignUpResponse;
import ceos.ipx.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import ceos.ipx.domain.auth.dto.ReissueResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;

@Tag(name = "Auth API", description = "인증/인가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "일반 회원가입", description = "이메일 인증이 완료된 사용자의 정보를 받아 계정을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 비밀번호 확인 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이메일 인증 미완료 또는 인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        SignUpResponse response = authService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인하고 JWT AccessToken을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비활성화된 계정"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpServletResponse
    ) {
        LoginResponse response = authService.login(request, httpServletResponse);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "AccessToken 재발급", description = "HttpOnly Cookie에 담긴 RefreshToken을 검증하여 새로운 AccessToken을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AccessToken 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "RefreshToken 누락 또는 유효하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비활성화된 계정"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpServletResponse
    ) {
        ReissueResponse response = authService.reissue(refreshToken, httpServletResponse);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}