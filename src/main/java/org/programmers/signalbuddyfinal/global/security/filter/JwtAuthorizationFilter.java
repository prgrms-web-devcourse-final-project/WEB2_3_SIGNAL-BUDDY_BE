package org.programmers.signalbuddyfinal.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtUtil;
import org.programmers.signalbuddyfinal.global.security.jwt.TokenErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final Set<String> excludeGetPaths = Set.of(
        "/api/feedbacks/{feedbackId}/comments", "/api/crossroads/**", "/api/feedbacks",
        "/feedbacks/**"
    );
    private final Set<String> excludeAllPaths = Set.of(
        "/", "/docs/**", "/ws/**", "/actuator/health", "/webjars/**", "/api/auth/login",
        "/docs/index.html", "/api/auth/reissue", "/api/members/join",
        "/api/admins/join", "/members/signup", "/api/members/files/**", "/actuator/prometheus"
    );

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (isExcludedPath(request)) {
            doFilter(request, response, filterChain);
            return;
        }

        if (request.getRequestURI().startsWith("/docs")) {
            log.info(request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractAccessToken(request);
        if (accessToken == null || accessToken.isEmpty()) {

            throw new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST);
        }

        jwtUtil.validateToken(accessToken);

        Authentication authentication = jwtUtil.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (excludeGetPaths.stream().anyMatch(pattern -> antPathMatcher.match(pattern, path))
            && method.equals("GET")) {
            return true;
        }
        if (excludeAllPaths.stream().anyMatch(pattern -> antPathMatcher.match(pattern, path))) {
            return true;
        }
        return false;
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST);
        }
        return bearerToken.substring(7);
    }


}
