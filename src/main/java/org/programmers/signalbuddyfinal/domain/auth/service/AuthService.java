package org.programmers.signalbuddyfinal.domain.auth.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtService;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getId(), loginRequest.getPassword()));

        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        HttpHeaders headers = new HttpHeaders();
        accessTokenSend2Client(headers, accessToken);
        refreshTokenSend2Client(headers, refreshToken);

        return ResponseEntity.ok()
            .headers(headers)
            .body(ApiResponse.createSuccessWithNoData());
    }

    public ResponseEntity<ApiResponse> reissue(String refreshToken) {
        NewTokenResponse newTokenResponse = jwtService.reissue(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        accessTokenSend2Client(headers, newTokenResponse.getAccessToken());
        refreshTokenSend2Client(headers, newTokenResponse.getRefreshToken());

        return ResponseEntity.ok()
            .headers(headers)
            .body(ApiResponse.createSuccessWithNoData());
    }

    private void accessTokenSend2Client(HttpHeaders headers, String accessToken) {
        headers.set("Authorization", "Bearer " + accessToken);
    }

    private void refreshTokenSend2Client(HttpHeaders headers, String refreshToken) {
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh-token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(7))
            .sameSite("Strict")
            .build();

        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
