package org.programmers.signalbuddyfinal.global.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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

    private static final String EXCEPTION_ATTRIBUTE = "exception";
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final Set<String> excludeGetPaths = Set.of(
        "/api/feedbacks/{feedbackId}/comments", "/api/crossroads/**", "/api/feedbacks"
    );
    private final Set<String> excludeAllPaths = Set.of(
        "/", "/docs/**", "/ws/**", "/actuator/health", "/webjars/**", "/api/auth/login",
        "/docs/index.html", "/api/members/join",
        "/api/admins/join", "/api/members/files/**", "/actuator/prometheus",
        "/api/auth/auth-code", "/api/auth/verify-code", "/api/auth/social-login"
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

        String accessToken = extractAccessToken(request);
        if (accessToken == null || accessToken.isEmpty()) {
            request.setAttribute(EXCEPTION_ATTRIBUTE, "EXPIRED_ACCESS_TOKEN");
            throw new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST);
        }

        try {
            jwtUtil.validateToken(accessToken);
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.info(e.getMessage());
            request.setAttribute(EXCEPTION_ATTRIBUTE, "INVALID_TOKEN");
            throw new BusinessException(TokenErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.info(e.getMessage());
            request.setAttribute(EXCEPTION_ATTRIBUTE, "EXPIRED_ACCESS_TOKEN");
            throw new BusinessException(TokenErrorCode.EXPIRED_ACCESS_TOKEN);
        }

        Authentication authentication = jwtUtil.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean isExcludedOnlyGetMethod = excludeGetPaths.stream()
            .anyMatch(pattern -> antPathMatcher.match(pattern, path) && method.equals("GET"));
        boolean isExcluded = excludeAllPaths.stream()
            .anyMatch(pattern -> antPathMatcher.match(pattern, path));

        return (isExcluded || isExcludedOnlyGetMethod);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST);
        }
        return bearerToken.substring(7);
    }


}
