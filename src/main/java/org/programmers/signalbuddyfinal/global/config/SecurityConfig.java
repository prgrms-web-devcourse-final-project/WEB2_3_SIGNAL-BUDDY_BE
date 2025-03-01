package org.programmers.signalbuddyfinal.global.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.global.security.CustomAuthenticationProvider;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetailsService;
import org.programmers.signalbuddyfinal.global.security.exception.CustomAuthenticationEntryPoint;
import org.programmers.signalbuddyfinal.global.security.filter.JwtAuthorizationFilter;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${url.front-url}")
    private String frontendUrl;
    @Value("${url.back-url}")
    private String backendUrl;

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter(JwtUtil jwtUtil) {
        return new JwtAuthorizationFilter(jwtUtil);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(customUserDetailsService, bCryptPasswordEncoder());
    }

    @Bean
    AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(customAuthenticationProvider()));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(configurationSource()));

        http
            .authorizeHttpRequests(auth -> auth
                // 문서화 api
                .requestMatchers("/",
                    "/docs/**",
                    "/ws/**",
                    "/actuator/health",
                    "/webjars/**").permitAll()
                // 로그인, 회원가입
                .requestMatchers("/api/auth/login","/api/auth/social-login", "/api/auth/reissue", "/api/members/join",
                    "/api/admins/join", "/api/members/files/**", "/api/auth/auth-code",
                    "api/auth/verify-code").permitAll()
                .requestMatchers("/api/bookmarks/**", "/bookmarks/**").hasRole("USER")
                // 댓글
                .requestMatchers(HttpMethod.GET, "/api/feedbacks/{feedbackId}/comments").permitAll()
                // 교차로
                .requestMatchers("/api/crossroads/save").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/crossroads/**").permitAll()
                // 피드백
                .requestMatchers(HttpMethod.GET, "/api/feedbacks/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/admin/feedbacks").hasRole(ADMIN)
                // 피드백 신고
                .requestMatchers(HttpMethod.GET, "/api/feedbacks/reports").hasRole(ADMIN)
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/feedbacks/{feedbackId}/reports/{reportId}"
                ).hasRole(ADMIN)
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/feedbacks/{feedbackId}/reports/{reportId}"
                ).hasRole(ADMIN)
                // 피드백 통계
                .requestMatchers("/api/feedback-summary/**").hasRole(ADMIN)
                // 회원
                .requestMatchers("/api/admins/**", "/admins/members/**").hasRole(ADMIN)
                .requestMatchers("/api/members/**", "/members/**").hasRole(USER)
                // Prometheus 엔드포인트 허용
                .requestMatchers("/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
            );

        // 기본 로그인 관련 설정
        http
            .formLogin(auth -> auth.disable())
            .httpBasic(auth -> auth.disable())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // csrf 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        http
            .addFilterAfter(jwtAuthorizationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(
            exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
            Arrays.asList(frontendUrl, backendUrl, backendUrl + ".nip.io", "http://localhost:3000",
                "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
