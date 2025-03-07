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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String EXCEPTION_ATTRIBUTE = "exception";
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final Set<String> excludeGetPaths = Set.of(
        "/api/feedbacks/{feedbackId}/comments", "/api/crossroads/**", "/api/feedbacks",
        "/api/crossroads/{crossroadId}/state", "/api/feedbacks/{feedbackId}"
    );
    private final Set<String> excludeAllPaths = Set.of(
        "/", "/docs/**", "/ws/**", "/actuator/health", "/webjars/**", "/api/auth/login",
        "/docs/index.html", "/api/members/join", "/docs/openapi3.yaml",
        "/api/admins/join", "/api/members/files/**", "/actuator/prometheus",
        "/api/auth/auth-code", "/api/auth/verify-code", "/api/members/password-reset",
        "/api/auth/social-login", "/api/members/restore", "/api/auth/reissue",
        "/api/auth/test/blacklist-expire", "/api/auth/test/time-expire/**"
    );

    public JwtAuthorizationFilter(JwtUtil jwtUtil, RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (isExcludedPath(request)) {
            doFilter(request, response, filterChain);
            return;
        }

        // 액세스 토큰 검증
        String accessToken = jwtUtil.extractAccessToken(request.getHeader("Authorization"));
        log.debug("Access token: {}", accessToken);
        if (accessToken == null || accessToken.isEmpty()) {
            request.setAttribute(EXCEPTION_ATTRIBUTE, "ACCESS_TOKEN_NOT_EXIST");
            throw new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST);
        }

        try {
            jwtUtil.parseToken(accessToken);
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.info(e.getMessage());
            request.setAttribute(EXCEPTION_ATTRIBUTE, "INVALID_TOKEN");
            throw new BusinessException(TokenErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.info(e.getMessage());
            request.setAttribute(EXCEPTION_ATTRIBUTE, "EXPIRED_ACCESS_TOKEN");
            throw new BusinessException(TokenErrorCode.EXPIRED_ACCESS_TOKEN);
        }

        // 블랙리스트에 있는지 확인
        if(checkBlacklist(accessToken)){

            request.setAttribute(EXCEPTION_ATTRIBUTE, "INVALID_TOKEN");
            throw new BusinessException(TokenErrorCode.INVALID_TOKEN);
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

    private boolean checkBlacklist(String accessToken) {
        Boolean isInBlackList = redisTemplate.hasKey("blacklist:access-token:" + accessToken);
        return Boolean.TRUE.equals(isInBlackList);
    }
}
