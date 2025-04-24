package org.programmers.signalbuddyfinal.global.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtUtil;
import org.programmers.signalbuddyfinal.global.security.jwt.TokenErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthorizationFilterTest {

    @InjectMocks
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
     private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    String path = "/api/members/1";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("유효한 accessToken이고, 인증이 성공하면 필터가 실행된다.")
    @Test
    void givenValidAccess_whenJwtAuthorizationFilterExecute_thenSuccess() throws ServletException, IOException {
        // given
        String accessToken = "valid.access.token";
        Authentication auth = mock(Authentication.class);

        when(request.getRequestURI()).thenReturn(path);
        when(request.getHeader("Authorization")).thenReturn("Bearer "+ accessToken);

        when(jwtUtil.extractAccessToken("Bearer " + accessToken)).thenReturn("valid.access.token");
        when(jwtUtil.parseToken(accessToken)).thenReturn(mock(Claims.class));
        when(jwtUtil.checkBlacklist(accessToken)).thenReturn(false);
        when(jwtUtil.getAuthentication(accessToken)).thenReturn(auth);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(auth);
    }

    @DisplayName("특정 경로에서 필터를 거치지 않는다.")
    @Test
    void givenSpecificPath_whenJwtAuthorizationFilterExecute_thenExecuteNextFilter() throws ServletException, IOException {
        // given
        path = "/api/members/join";
        when(request.getRequestURI()).thenReturn(path);

        // when
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("AccessToken이 없어서 에러가 발생한다")
    @Test
    void givenNonExistentAccessToken_whenJwtAuthorizationFilterExecute_thenThrowsAccessTokenNotExistError() throws ServletException, IOException {
        // given
        String accessToken = null;
        when(request.getRequestURI()).thenReturn(path);
        when(jwtUtil.extractAccessToken(accessToken)).thenThrow(new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST));

        // when & then
        assertThatThrownBy(()->jwtAuthorizationFilter.doFilterInternal(request, response, filterChain))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST.getMessage());

        verify(request).setAttribute("exception", "ACCESS_TOKEN_NOT_EXIST");
    }

    @DisplayName("올바르지 않은 토큰으로 에러가 발생한다.")
    @Test
    void givenInvalidAccessToken_whenJwtAuthorizationFilterExecute_thenThrowsInvalidTokenError() throws ServletException, IOException {
        // given
        String accessToken = "invalid.access.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        when(request.getRequestURI()).thenReturn(path);
        when(jwtUtil.extractAccessToken("Bearer " + accessToken)).thenReturn(accessToken);
        when(jwtUtil.parseToken(accessToken)).thenThrow(
            new MalformedJwtException(TokenErrorCode.INVALID_TOKEN.getMessage()));

        // when & then
        assertThatThrownBy(
            () -> jwtAuthorizationFilter.doFilterInternal(request, response, filterChain))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(TokenErrorCode.INVALID_TOKEN.getMessage());
    }

    @DisplayName("AccessToken이 만료되어 에러가 발생한다.")
    @Test
    void givenExpiredAccessToken_whenJwtAuthorizationFilterExecute_thenThrowsExpiredAccessTokenError() throws ServletException, IOException {
        // given
        String accessToken = "expired.access.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        when(request.getRequestURI()).thenReturn(path);
        when(jwtUtil.extractAccessToken("Bearer " + accessToken)).thenReturn(accessToken);
        when(jwtUtil.parseToken(accessToken)).thenThrow(new ExpiredJwtException(mock(Header.class),mock(Claims.class),TokenErrorCode.EXPIRED_ACCESS_TOKEN.getMessage()));

        // when & then
        assertThatThrownBy(()->jwtAuthorizationFilter.doFilterInternal(request, response, filterChain))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(TokenErrorCode.EXPIRED_ACCESS_TOKEN.getMessage());
    }

    @DisplayName("AccessToken이 블랙리스트에 존재하여 에러가 발생한다.")
    @Test
    void givenBlacklistAccessToken_whenJwtAuthorization_thenThrowsInvalidTokenError() throws ServletException, IOException {
        // given
        String accessToken = "blacklist.access.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        when(request.getRequestURI()).thenReturn(path);
        when(jwtUtil.extractAccessToken("Bearer " + accessToken)).thenReturn(accessToken);
        when(jwtUtil.parseToken(accessToken)).thenReturn(mock(Claims.class));
        when(jwtUtil.checkBlacklist(accessToken)).thenReturn(true);

        // when & then
        assertThatThrownBy(()->jwtAuthorizationFilter.doFilterInternal(request, response, filterChain))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(TokenErrorCode.INVALID_TOKEN.getMessage());

        verify(request).setAttribute("exception", "INVALID_TOKEN");
    }

}
