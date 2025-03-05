package org.programmers.signalbuddyfinal.domain.auth.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.mapper.MemberMapper;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
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

    // 토큰 재발행
    public ResponseEntity<ApiResponse<Object>> reissue(String refreshToken, String accessToken) {
        NewTokenResponse newTokenResponse = jwtService.reissue(refreshToken, accessToken);
        HttpHeaders headers = new HttpHeaders();
        accessTokenSend2Client(headers, newTokenResponse.getAccessToken());
        refreshTokenSend2Client(headers, newTokenResponse.getRefreshToken(), 7);

        return ResponseEntity.ok()
            .headers(headers)
            .body(ApiResponse.createSuccessWithNoData());
    }

    // 기본 로그인
    public ResponseEntity<ApiResponse<MemberResponse>> login(LoginRequest loginRequest) {

        return commonLogin(loginRequest.getId(), loginRequest.getPassword());
    }

    // 소셜 로그인
    public ResponseEntity<ApiResponse<MemberResponse>> socialLogin(
        SocialLoginRequest socialLoginRequest) {

        Member existMember = memberRepository.findByProviderAndSocialId(
                socialLoginRequest.getProvider(), socialLoginRequest.getSocialUserId())
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        return commonLogin(existMember.getEmail(), null);
    }

    // 공통 로그인 로직
    private ResponseEntity<ApiResponse<MemberResponse>> commonLogin(String email, String password) {

        Authentication authentication = createAuthentication(email, password);

        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        HttpHeaders headers = new HttpHeaders();
        accessTokenSend2Client(headers, accessToken);
        refreshTokenSend2Client(headers, refreshToken, 7);

        return ResponseEntity.ok()
            .headers(headers)
            .body(ApiResponse.createSuccess(createResponseBody(authentication)));
    }

    public ResponseEntity<ApiResponse<Object>> logout(String refreshToken, String accessToken) {

        jwtService.logout(accessToken);

        HttpHeaders headers = new HttpHeaders();
        refreshTokenSend2Client(headers, refreshToken, 0);

        return ResponseEntity.ok()
            .headers(headers)
            .body(ApiResponse.createSuccessWithNoData());
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
