package org.programmers.signalbuddyfinal.domain.auth.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.auth.dto.EmailRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.LogoutResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.ReissueResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.VerifyCodeRequest;
import org.programmers.signalbuddyfinal.domain.auth.entity.Purpose;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.domain.social.entity.SocialProvider;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtService;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private FcmService fcmService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    EmailService emailService;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    private Member member;
    private String deviceTokenCookie = "deviceToken";
    private CustomUserDetails customUserDetails;
    private Authentication authentication;

    @BeforeEach
    void setup() {
        member = createMember(MemberStatus.ACTIVITY);
        customUserDetails = new CustomUserDetails(member);

        authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null,
            customUserDetails.getAuthorities());
    }

    @Nested
    @DisplayName("기본 로그인")
    class whenBasicLogin {

        @DisplayName("기본 로그인에 성공한다.")
        @Test
        void givenRightData_whenBasicLogin_thenSuccess() {
            // given
            LoginRequest loginRequest = new LoginRequest(member.getEmail(), member.getPassword());

            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            // when
            LoginResponse actualLoginResponse = authService.login(deviceTokenCookie, loginRequest);

            // then
            assertThat(actualLoginResponse.getMemberResponse().getEmail()).isEqualTo(
                member.getEmail());
            assertThat(actualLoginResponse.getHttpHeaders().get("Authorization")).isNotNull();
            assertThat(actualLoginResponse.getHttpHeaders().getFirst("Set-Cookie")).contains(
                "refresh-token");
        }

        @DisplayName("탈퇴한 회원으로 기본 로그인에 실패한다.")
        @Test
        void givenWithdrawalMember_whenBasicLogin_thenReturnWithdrawnMemberMessage() {
            // given
            Member withdrawalMember = createMember(MemberStatus.WITHDRAWAL);

            LoginRequest loginRequest = new LoginRequest(withdrawalMember.getEmail(),
                withdrawalMember.getPassword());
            when(authenticationManager.authenticate(any())).thenThrow(
                new BusinessException(MemberErrorCode.WITHDRAWN_MEMBER));

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
            LoginRequest loginRequest = new LoginRequest(member.getEmail(), "wrongPassword");
            when(authenticationManager.authenticate(any()))
                .thenThrow(new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

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

            when(authenticationManager.authenticate(any())).thenThrow(
                new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

            // when
            LoginResponse actualLoginResponse = authService.login(deviceTokenCookie, loginRequest);

            // then
            assertThat(actualLoginResponse.getMessage()).contains(
                MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Nested
    @DisplayName("소셜 로그인")
    class whenSocialLogin {

        private SocialProvider savedSocialProvider;

        @BeforeEach
        void socialLoginSetup() {
            savedSocialProvider = SocialProvider.builder()
                .oauthProvider(Provider.GOOGLE)
                .socialId("socialUUID")
                .member(member)
                .build();
        }

        @DisplayName("소셜 로그인에 성공한다.")
        @Test
        void givenRightData_whenSocialLogin_thenSuccess() {
            // given
            SocialLoginRequest socialLoginRequest = new SocialLoginRequest(
                savedSocialProvider.getOauthProvider(), savedSocialProvider.getSocialId());

            when(memberRepository.findByProviderAndSocialId(socialLoginRequest.getProvider(),
                socialLoginRequest.getSocialUserId())).thenReturn(
                Optional.of(member));

            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            // when
            LoginResponse actualLoginResponse = authService.socialLogin(deviceTokenCookie,
                socialLoginRequest);

            // then
            assertThat(actualLoginResponse.getMemberResponse().getEmail()).isEqualTo(
                member.getEmail());
            assertThat(actualLoginResponse.getHttpHeaders().get("Authorization")).isNotNull();
            assertThat(actualLoginResponse.getHttpHeaders().getFirst("Set-Cookie")).contains(
                "refresh-token");
        }


        @DisplayName("회원가입되지 않은 계정으로 소셜 로그인에 실패한다.")
        @Test
        void givenNotExistentAccount_whenSocialLogin_thenReturnNotFoundMemberMessage() {
            // given
            SocialLoginRequest socialLoginRequest = new SocialLoginRequest(Provider.GOOGLE,
                "not-exist-provider");

            when(memberRepository.findByProviderAndSocialId(socialLoginRequest.getProvider(),
                socialLoginRequest.getSocialUserId())).thenReturn(
                Optional.empty());

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
            Member withdrawalSocialAccount = createMember(MemberStatus.WITHDRAWAL);

            SocialLoginRequest socialLoginRequest = new SocialLoginRequest(
                savedSocialProvider.getOauthProvider(), savedSocialProvider.getSocialId());

            when(memberRepository.findByProviderAndSocialId(socialLoginRequest.getProvider(),
                socialLoginRequest.getSocialUserId())).thenReturn(
                Optional.of(withdrawalSocialAccount));

            when(authenticationManager.authenticate(any())).thenThrow(
                new BusinessException(MemberErrorCode.WITHDRAWN_MEMBER));

            // when
            LoginResponse actualLoginResponse = authService.socialLogin(deviceTokenCookie,
                socialLoginRequest);

            // then
            assertThat(actualLoginResponse.getMessage()).contains(
                MemberErrorCode.WITHDRAWN_MEMBER.getMessage());
        }
    }

    @Nested
    @DisplayName("토큰 재발행")
    class whenReissueToken {

        String originRefreshToken = "origin-refresh-token";
        String originAccessToken = "origin-access-token";
        String reissueRefreshToken = "reissue-refresh-token";
        String reissueAccessToken = "reissue-access-token";

        @DisplayName("토큰 재발행에 성공한다.")
        @Test
        void givenValidTokens_whenReissue_thenReturnNewTokens() {
            // given
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
            when(jwtService.reissue(originAccessToken, null)).thenThrow(new BusinessException(
                AuthErrorCode.UNAUTHORIZED));

            // when & then
            assertThatThrownBy(() -> authService.reissue(originAccessToken, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(AuthErrorCode.UNAUTHORIZED.getMessage());

            verify(jwtService, times(1)).reissue(originAccessToken, null);
        }
    }

    @DisplayName("로그아웃에 성공한다.")
    @Test
    void givenValidToken_whenLogout_thenSuccess() {
        // given
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        doNothing().when(jwtService).logout(accessToken);
        doNothing().when(fcmService).logoutToken(anyString());

        // when
        LogoutResponse actualLogoutResponse = authService.logout(deviceTokenCookie, accessToken,
            refreshToken);

        // then
        verify(jwtService, times(1)).logout(accessToken);

        String afterLogoutSetCookieHeader = actualLogoutResponse.getHttpHeaders()
            .getFirst("Set-Cookie");

        assertThat(afterLogoutSetCookieHeader).contains("Max-Age=0");
    }

    @Nested
    @DisplayName("이메일 검증")
    class whenVerifyEmail {

        @Test
        @DisplayName("이메일이 존재하는 경우, EmailService의 sendEmail을 호출한다.")
        void givenExistedEmail_whenEmailVerification_thenCallEmailService() {
            //given
            when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
            doNothing().when(emailService).sendEmail(member.getEmail());

            //when
            authService.emailVerification(new EmailRequest(member.getEmail()));

            //then
            verify(emailService, times(1)).sendEmail(member.getEmail());
        }

        @Test
        @DisplayName("이메일이 존재하지 않는 경우, NotFoundMember를 던진다.")
        void givenNotExistedMember_whenEmailVerification_thenThrowsNotFoundError() {
            //given
            when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());

            //when
            assertThatThrownBy(
                () -> authService.emailVerification(new EmailRequest(member.getEmail())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());

            verify(emailService, times(0)).sendEmail(member.getEmail());
        }

        @Test
        @DisplayName("탈퇴한 회원인 경우, NotFoundMember를 던진다.")
        void givenWithdrawalMember_whenEmailVerification_thenThrowsNotFoundError() {
            //given
            Member withdrawalMember = createMember(MemberStatus.WITHDRAWAL);
            when(memberRepository.findByEmail(withdrawalMember.getEmail())).thenReturn(
                Optional.of(withdrawalMember));

            //when
            assertThatThrownBy(
                () -> authService.emailVerification(new EmailRequest(withdrawalMember.getEmail())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());

            verify(emailService, times(0)).sendEmail(member.getEmail());
        }
    }

    @Nested
    @DisplayName("인증 코드 검증")
    class whenVerifyCode {

        VerifyCodeRequest verifyCodeRequest = new VerifyCodeRequest(Purpose.NEW_PASSWORD,
            "test@test.com", "123456");

        @BeforeEach
        void verifyCodeSetup() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
        }

        @Test
        @DisplayName("인증에 성공한다.")
        void givenValidCode_whenVerifyCode_thenReturnVoid() {
            // given
            when(valueOperations.get(any())).thenReturn(verifyCodeRequest.getCode());
            when(redisTemplate.delete(anyString())).thenReturn(true);
            doNothing().when(valueOperations).set(any(), any(), any(Long.class), any());

            // when
            authService.verifyCode(verifyCodeRequest);

            // then
            verify(redisTemplate, times(1)).delete(anyString());
            verify(valueOperations, times(1)).set(anyString(), anyString(), anyLong(), any());
        }

        @Test
        @DisplayName("인증 유효 시간이 끝나서 INVALID_AUTH_CODE 에러를 반환한다.")
        void givenAuthExpirationTimeIsEnd_whenVerifyCode_thenThrowsInvalidAuthCodeError() {
            // given
            when(valueOperations.get(any())).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.verifyCode(verifyCodeRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(AuthErrorCode.INVALID_AUTH_CODE.getMessage());
        }

        @Test
        @DisplayName("인증코드가 일치하지 않아 NOT_MATCH_AUTH_CODE 에러를 반환한다.")
        void givenNotMatchedAuthCode_whenVerifyCode_thenThrowsNotMatchAuthCodeError() {
            // given
            when(valueOperations.get(any())).thenReturn("wrong");

            // when & then
            assertThatThrownBy(() -> authService.verifyCode(verifyCodeRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(AuthErrorCode.NOT_MATCH_AUTH_CODE.getMessage());
        }
    }

    private Member createMember(MemberStatus status) {
        return Member.builder()
            .email("test@test.com")
            .nickname("테스트")
            .profileImageUrl("프로필 경로")
            .role(MemberRole.USER)
            .memberStatus(status)
            .password("password")
            .build();
    }
}
