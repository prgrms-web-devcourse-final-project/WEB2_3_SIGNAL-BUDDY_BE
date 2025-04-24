package org.programmers.signalbuddyfinal.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    String originAccessToken = "originAccessToken";
    String originRefreshToken = "originRefreshToken";
    Claims accessTokenClaims;
    Claims refreshTokenClaims;

    Authentication authentication;

    @BeforeEach
    void setup(){
        Member member = Member.builder()
            .email("MembeForJwtService@test.com")
            .nickname("JwtService 테스트용")
            .profileImageUrl("프로필 경로")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        accessTokenClaims = mock(Claims.class);
        refreshTokenClaims = mock(Claims.class);
    }

    @DisplayName("토큰 재발행 성공")
    @Test
    void givenValidTokens_whenTokenReissue_thenReissueSuccess() {
        // given
        String reissueAccessToken = "reissueAccessToken";
        String reissueRefreshToken = "reissueRefreshToken";

        when(accessTokenClaims.getSubject()).thenReturn("1");
        when(refreshTokenClaims.getSubject()).thenReturn("1");

        when(jwtUtil.extractAccessToken("Bearer " + originAccessToken)).thenReturn(originAccessToken);
        when(jwtUtil.extractClaimsOrThrow(eq("accessToken"), anyString())).thenReturn(accessTokenClaims);
        when(jwtUtil.extractClaimsOrThrow(eq("refreshToken"), anyString())).thenReturn(refreshTokenClaims);
        when(jwtUtil.checkBlacklist(anyString())).thenReturn(false);
        doNothing().when(jwtUtil).validateAccessTokenExpiration(any(Claims.class), anyString());
        when(jwtUtil.getAuthentication(anyString())).thenReturn(authentication);
        when(jwtUtil.generateAccessToken(any(Authentication.class))).thenReturn(reissueAccessToken);
        when(jwtUtil.generateRefreshToken(any(Authentication.class))).thenReturn(reissueRefreshToken);

        when(refreshTokenRepository.findByMemberId(any())).thenReturn(originRefreshToken);

        // when
        NewTokenResponse response = jwtService.reissue("Bearer "+ originAccessToken, originRefreshToken);

        // then
        assertNotNull(response);
        assertEquals(response.getAccessToken(), reissueAccessToken);
        assertEquals(response.getRefreshToken(), reissueRefreshToken);
    }

    @DisplayName("토큰 재발행 실패: refreshToken과 accessToken에 존재하는 memberId가 불일치")
    @Test
    void givenDifferentMemberIdFromToken_whenTokenReissue_thenThrowsBadRequestError() {
        // given
        when(accessTokenClaims.getSubject()).thenReturn("1");
        when(refreshTokenClaims.getSubject()).thenReturn("2");

        when(jwtUtil.extractAccessToken("Bearer " + originAccessToken)).thenReturn(originAccessToken);
        when(jwtUtil.extractClaimsOrThrow(eq("accessToken"), anyString())).thenReturn(accessTokenClaims);
        when(jwtUtil.extractClaimsOrThrow(eq("refreshToken"), anyString())).thenReturn(refreshTokenClaims);

        // when & then
        assertThatThrownBy(() -> jwtService.reissue("Bearer " + originAccessToken, originRefreshToken))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(GlobalErrorCode.BAD_REQUEST.getMessage());
    }

    @DisplayName("토큰 재발행 실패: accessToken이 블랙리스트에 존재")
    @Test
    void givenBlackListAccessToken_whenTokenReissue_thenThrowsBadRequestError() {
        // given
        when(accessTokenClaims.getSubject()).thenReturn("1");
        when(refreshTokenClaims.getSubject()).thenReturn("1");

        when(jwtUtil.extractAccessToken("Bearer " + originAccessToken)).thenReturn(originAccessToken);
        when(jwtUtil.extractClaimsOrThrow(eq("accessToken"), anyString())).thenReturn(accessTokenClaims);
        when(jwtUtil.extractClaimsOrThrow(eq("refreshToken"), anyString())).thenReturn(refreshTokenClaims);
        when(jwtUtil.checkBlacklist(anyString())).thenReturn(true);

        when(refreshTokenRepository.findByMemberId(any())).thenReturn(originRefreshToken);

        // when & then
        assertThatThrownBy(() -> jwtService.reissue("Bearer " + originAccessToken, originRefreshToken))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(GlobalErrorCode.BAD_REQUEST.getMessage());
    }

    @DisplayName("토큰 재발행 실패: refreshToken이 없음")
    @Test
    void givenNonExistentRefreshToken_whenTokenReissue_thenThrowsUnauthorizedError() {
        // given
        when(accessTokenClaims.getSubject()).thenReturn("1");
        when(refreshTokenClaims.getSubject()).thenReturn("1");

        when(jwtUtil.extractAccessToken("Bearer " + originAccessToken)).thenReturn(originAccessToken);
        when(jwtUtil.extractClaimsOrThrow(eq("accessToken"), anyString())).thenReturn(accessTokenClaims);
        when(jwtUtil.extractClaimsOrThrow(eq("refreshToken"), anyString())).thenReturn(refreshTokenClaims);
        when(jwtUtil.checkBlacklist(anyString())).thenReturn(false);
        doNothing().when(jwtUtil).validateAccessTokenExpiration(any(Claims.class), anyString());

        when(refreshTokenRepository.findByMemberId(any())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> jwtService.reissue("Bearer " + originAccessToken, originRefreshToken))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(AuthErrorCode.UNAUTHORIZED.getMessage());
    }

    @DisplayName("로그아웃 성공")
    @Test
    void givenValidToken_whenLogout_thenLogoutSuccess(){
        // given
        when(jwtUtil.extractAccessToken(originAccessToken)).thenReturn("originAccessToken");
        when(jwtUtil.extractClaimsOrThrow(anyString(), anyString())).thenReturn(accessTokenClaims);
        when(accessTokenClaims.getExpiration()).thenReturn(new Date());
        when(accessTokenClaims.getSubject()).thenReturn("1");
        doNothing().when(jwtUtil).addBlackListExistingAccessToken(anyString(), any(Date.class));

        when(refreshTokenRepository.findByMemberId(anyString())).thenReturn("refreshToken");

        // when
        jwtService.logout(originAccessToken);

        // then
        verify(refreshTokenRepository, times(1)).delete(anyString());
    }

}