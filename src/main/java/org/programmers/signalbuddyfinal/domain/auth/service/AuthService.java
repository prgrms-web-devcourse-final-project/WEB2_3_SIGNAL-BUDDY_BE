package org.programmers.signalbuddyfinal.domain.auth.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.LogoutResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.ReissueResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.mapper.MemberMapper;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtService;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final FcmService fcmService;

    // 토큰 재발행
    public ReissueResponse reissue(String refreshToken, String accessToken) {
        NewTokenResponse newTokenResponse = jwtService.reissue(refreshToken, accessToken);
        HttpHeaders headers = new HttpHeaders();
        accessTokenSend2Client(headers, newTokenResponse.getAccessToken());
        refreshTokenSend2Client(headers, newTokenResponse.getRefreshToken(), 7);

        return new ReissueResponse(headers);
    }

    // 기본 로그인
    public LoginResponse login(
        String deviceTokenCookie,
        LoginRequest loginRequest
    ) {
        return commonLogin(deviceTokenCookie, loginRequest.getId(), loginRequest.getPassword());
    }

    // 소셜 로그인
    public LoginResponse socialLogin(
        String deviceToken,
        SocialLoginRequest socialLoginRequest) {

        Member existMember = memberRepository.findByProviderAndSocialId(
                socialLoginRequest.getProvider(), socialLoginRequest.getSocialUserId())
            .orElse(null);

        if (existMember == null) {
            return LoginResponse.fail(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }

        return commonLogin(deviceToken, existMember.getEmail(), null);
    }

    // 공통 로그인 로직
    private LoginResponse commonLogin(
        String deviceTokenCookie,
        String email, String password
    ) {

        Authentication authentication = null;
        try {
            authentication = createAuthentication(email, password);
        } catch (BusinessException e) {
            return LoginResponse.fail(e.getErrorCode().getMessage());
        }

        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        HttpHeaders headers = new HttpHeaders();
        accessTokenSend2Client(headers, accessToken);
        refreshTokenSend2Client(headers, refreshToken, 7);

        fcmService.loginToken(deviceTokenCookie);

        return LoginResponse.success(headers, createResponseBody(authentication));
    }

    public LogoutResponse logout(
        String deviceTokenCookie,
        String accessToken, String refreshToken
        ) {
        jwtService.logout(accessToken);
        fcmService.logoutToken(deviceTokenCookie);

        HttpHeaders headers = new HttpHeaders();
        refreshTokenSend2Client(headers, refreshToken, 0);

        return new LogoutResponse(headers);
    }

    // Authentication 객체 생성
    private Authentication createAuthentication(String email, String password) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));
    }

    // AccessToken을 Authorization 헤더에 설정
    private void accessTokenSend2Client(HttpHeaders headers, String accessToken) {
        headers.set("Authorization", "Bearer " + accessToken);
    }

    // RefreshToken을 Set-Cookie 헤더에 설정
    private void refreshTokenSend2Client(HttpHeaders headers, String refreshToken, long duration) {
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh-token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(duration))
            .sameSite("None")
            .build();

        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    // Authentication 객체를 MemberResponse 객체로 변환
    private MemberResponse createResponseBody(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Member loginMember = Member.builder()
            .memberId(userDetails.getMemberId())
            .email(userDetails.getEmail())
            .nickname(userDetails.getNickname())
            .profileImageUrl(userDetails.getProfileImageUrl())
            .role(userDetails.getRole())
            .memberStatus(userDetails.getStatus())
            .build();

        return MemberMapper.INSTANCE.toDto(loginMember);
    }
}
