package org.programmers.signalbuddyfinal.domain.auth.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.LogoutResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.ReissueResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.domain.social.entity.SocialProvider;
import org.programmers.signalbuddyfinal.domain.social.repository.SocialProviderRepository;
import org.programmers.signalbuddyfinal.global.db.RedisTestContainer;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.CustomAuthenticationProvider;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtService;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AuthServiceTest extends ServiceTest implements RedisTestContainer {

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SocialProviderRepository socialProviderRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private JwtService jwtService;


    @MockitoBean
    private FcmService fcmService;

    private Member savedMember;
    private SocialProvider savedSocialProvider;
    private String deviceTokenCookie = "deviceToken";

    @BeforeEach
    void setup() {
        Member member = Member.builder()
            .email("loginMember@test.com")
            .nickname("로그인 테스트용")
            .profileImageUrl("프로필 경로")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .password(bCryptPasswordEncoder.encode("password"))
            .build();

        SocialProvider socialProvider = SocialProvider.builder()
            .oauthProvider(Provider.GOOGLE)
            .socialId("socialUUID")
            .member(member)
            .build();

        savedMember = memberRepository.save(member);
        savedSocialProvider = socialProviderRepository.save(socialProvider);
        doNothing().when(fcmService).loginToken(anyString());
    }

    @DisplayName("기본 로그인에 성공한다.")
    @Test
    void givenRightData_whenBasicLogin_thenSuccess() {
        // given
        LoginRequest loginRequest = new LoginRequest(savedMember.getEmail(), "password");

        // when
        LoginResponse actualLoginResponse = authService.login(deviceTokenCookie, loginRequest);

        // then
        assertThat(actualLoginResponse.getMemberResponse().getEmail()).isEqualTo(
            savedMember.getEmail());
        assertThat(actualLoginResponse.getHttpHeaders().get("Authorization")).isNotNull();
        assertThat(actualLoginResponse.getHttpHeaders().getFirst("Set-Cookie")).contains(
            "refresh-token");
    }

    @DisplayName("소셜 로그인에 성공한다.")
    @Test
    void givenRightData_whenSocialLogin_thenSuccess() {
        // given
        SocialLoginRequest socialLoginRequest = new SocialLoginRequest(
            savedSocialProvider.getOauthProvider(), savedSocialProvider.getSocialId());

        // when
        LoginResponse actualLoginResponse = authService.socialLogin(deviceTokenCookie,
            socialLoginRequest);

        // then
        assertThat(actualLoginResponse.getMemberResponse().getEmail()).isEqualTo(
            savedMember.getEmail());
        assertThat(actualLoginResponse.getHttpHeaders().get("Authorization")).isNotNull();
        assertThat(actualLoginResponse.getHttpHeaders().getFirst("Set-Cookie")).contains(
            "refresh-token");
    }

    @DisplayName("탈퇴한 회원으로 기본 로그인에 실패한다.")
    @Test
    void givenWithdrawalMember_whenBasicLogin_thenReturnWithdrawnMemberMessage() {
        // given
        Member withdrawalMember = Member.builder()
            .email("withdrawal@test.com")
            .nickname("탈퇴 회원 로그인 테스트용")
            .profileImageUrl("프로필 경로")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.WITHDRAWAL)
            .password(bCryptPasswordEncoder.encode("password"))
            .build();

        memberRepository.save(withdrawalMember);
        LoginRequest loginRequest = new LoginRequest(withdrawalMember.getEmail(), "password");

        // when
        LoginResponse actualLoginResponse = authService.login(deviceTokenCookie, loginRequest);

        // then
        assertThat(actualLoginResponse.getMessage()).contains(
            MemberErrorCode.WITHDRAWN_MEMBER.getMessage());
    }

    @DisplayName("비밀번호 불일치로 기본 로그인에 실패한다.")
    @Test
    void givenNotMatchedPassword_whenBasicLogin_thenReturnNotFoundMemberMessage() {
        // given
        LoginRequest loginRequest = new LoginRequest(savedMember.getEmail(), "wrongPassword");

        // when
        LoginResponse actualLoginResponse = authService.login(deviceTokenCookie, loginRequest);

        // then
        assertThat(actualLoginResponse.getMessage()).contains(
            MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @DisplayName("회원가입되지 않은 계정으로 기본 로그인에 실패한다.")
    @Test
    void givenNotExistentAccount_whenBasicLogin_thenReturnNotFoundMemberMessage() {
        // given
        String email = "not-exist-email@test.com";
        String password = "password";
        LoginRequest loginRequest = new LoginRequest(email, password);

        // when
        LoginResponse actualLoginResponse = authService.login(deviceTokenCookie, loginRequest);

        // then
        assertThat(actualLoginResponse.getMessage()).contains(
            MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @DisplayName("회원가입되지 않은 계정으로 소셜 로그인에 실패한다.")
    @Test
    void givenNotExistentAccount_whenSocialLogin_thenReturnNotFoundMemberMessage() {
        // given
        SocialLoginRequest socialLoginRequest = new SocialLoginRequest(Provider.GOOGLE,
            "not-exist-provider");

        // when
        LoginResponse actualLoginResponse = authService.socialLogin(deviceTokenCookie,
            socialLoginRequest);

        // then
        assertThat(actualLoginResponse.getMessage()).contains(
            MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @DisplayName("탈퇴한 회원으로 소셜 로그인에 실패한다.")
    @Test
    void givenWithdrawalMember_whenSocialLogin_thenReturnWithdrawnMemberMessage() {
        // given
        Member withdrawalSocialAccount = Member.builder()
            .email("withdrawal@test.com")
            .nickname("탈퇴 회원 로그인 테스트용")
            .profileImageUrl("프로필 경로")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.WITHDRAWAL)
            .password(bCryptPasswordEncoder.encode("password"))
            .build();

        memberRepository.save(withdrawalSocialAccount);

        SocialProvider withdrawalSocialProvider = SocialProvider.builder()
            .oauthProvider(Provider.GOOGLE)
            .socialId("withdrawalUUID")
            .member(withdrawalSocialAccount)
            .build();
        socialProviderRepository.save(withdrawalSocialProvider);

        SocialLoginRequest socialLoginRequest = new SocialLoginRequest(
            withdrawalSocialProvider.getOauthProvider(), withdrawalSocialProvider.getSocialId());

        // when
        LoginResponse actualLoginResponse = authService.socialLogin(deviceTokenCookie,
            socialLoginRequest);

        // then
        assertThat(actualLoginResponse.getMessage()).contains(
            MemberErrorCode.WITHDRAWN_MEMBER.getMessage());
    }

    @DisplayName("토큰 재발행에 성공한다.")
    @Test
    void givenValidTokens_whenReissue_thenReturnNewTokens() {
        // given
        String originRefreshToken = "origin-refresh-token";
        String originAccessToken = "origin-access-token";
        String reissueRefreshToken = "reissue-refresh-token";
        String reissueAccessToken = "reissue-access-token";

        NewTokenResponse newTokenResponse = new NewTokenResponse(reissueAccessToken,
            reissueRefreshToken);
        when(jwtService.reissue(originAccessToken, originRefreshToken)).thenReturn(
            newTokenResponse);

        // when
        ReissueResponse actualNewTokenResponse = authService.reissue(originAccessToken,
            originRefreshToken);

        // then
        verify(jwtService, times(1)).reissue(originAccessToken, originRefreshToken);
        assertThat(actualNewTokenResponse.getHttpHeaders().getFirst("Authorization")).contains(
            reissueAccessToken);
        assertThat(actualNewTokenResponse.getHttpHeaders().getFirst("Set-Cookie")).contains(
            reissueRefreshToken);
    }

    @DisplayName("리프레시 토큰이 없어 토큰 재발행에 실패한다.")
    @Test
    void givenRefreshTokenIsNull_whenReissue_thenThrowsUnauthorizedError() {
        // given
        String originAccessToken = "origin-access-token";

        when(jwtService.reissue(originAccessToken, null)).thenThrow(new BusinessException(
            AuthErrorCode.UNAUTHORIZED));

        // when & then
        assertThatThrownBy(() -> authService.reissue(originAccessToken, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(AuthErrorCode.UNAUTHORIZED.getMessage());

        verify(jwtService, times(1)).reissue(originAccessToken, null);
    }

    @DisplayName("로그아웃에 성공한다.")
    @Test
    void givenValidToken_whenLogout_thenSuccess(){
        // given
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        doNothing().when(jwtService).logout(accessToken);
        doNothing().when(fcmService).logoutToken(anyString());

        // when
        LogoutResponse actualLogoutResponse = authService.logout(deviceTokenCookie, accessToken, refreshToken);

        // then
        verify(jwtService, times(1)).logout(accessToken);

        String afterLogoutSetCookieHeader = actualLogoutResponse.getHttpHeaders().getFirst("Set-Cookie");

        assertThat(afterLogoutSetCookieHeader).contains("Max-Age=0");
    }
}
