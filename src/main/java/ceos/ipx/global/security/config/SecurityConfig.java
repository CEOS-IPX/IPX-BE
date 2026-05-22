package ceos.ipx.global.security.config;

import ceos.ipx.global.security.handler.CustomAccessDeniedHandler;
import ceos.ipx.global.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    // private final JwtAuthenticationFilter jwtAuthenticationFilter;   // JWT 필터 구현 후 주입

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ===== CORS 설정 =====
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ===== CSRF 비활성화 =====
                // JWT 기반 stateless 인증 사용 -> 쿠키가 아닌 헤더를 사용하므로 CSRF 공격으로부터 안전
                .csrf(AbstractHttpConfigurer::disable)

                // ===== 세션 정책: STATELESS =====
                // JWT는 stateless -> 서버는 세션 사용 X
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ===== HTTP Basic / Form Login, Logout 비활성화 =====
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // ===== 경로별 권한 설정 =====
                .authorizeHttpRequests(auth -> auth

                        // [permitAll]
                        .requestMatchers(SecurityWhitelist.PERMIT_ALL_PATHS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // [authenticated]
                        .requestMatchers(SecurityWhitelist.AUTHENTICATED_PATHS).authenticated()

                        // [기본 정책] 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        // ===== JWT 필터 추가 =====
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용 도메인
        config.setAllowedOrigins(List.of(
                "http://localhost:3000"            // 프론트 개발 환경
//                "https://?????.com"          // 운영 도메인
        ));

        // 허용 HTTP 메서드
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용 헤더 (모든 헤더 허용)
        config.setAllowedHeaders(List.of("*"));

        // 클라이언트에서 응답으로 받을 수 있는 헤더
        config.setExposedHeaders(List.of(
                "Authorization",
                "Set-Cookie"
        ));

        // 인증 정보 전송 허용 (쿠키, Authorization 헤더)
        config.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (초)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}