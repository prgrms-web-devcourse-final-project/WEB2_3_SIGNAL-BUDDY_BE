package org.programmers.signalbuddyfinal.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.auth.dto.EmailRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.LogoutResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.ReissueResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.VerifyCodeRequest;
import org.programmers.signalbuddyfinal.domain.auth.service.AuthService;
import org.programmers.signalbuddyfinal.domain.auth.service.EmailService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(
        @CookieValue(name = "device-token", required = false) String deviceTokenCookie,
        @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse loginResponse = authService.login(deviceTokenCookie, loginRequest);

        // 로그인 성공
        if (loginResponse.getMemberResponse() != null) {
            return ResponseEntity.ok()
                .headers(loginResponse.getHttpHeaders())
                .body(ApiResponse.createSuccess(loginResponse.getMemberResponse()));
        }

        // 로그인 실패
        else {
            return ResponseEntity.ok()
                .body(ApiResponse.createError(loginResponse.getMessage()));
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Object>> reissue(
        @RequestHeader("Authorization") String accessToken,
        @CookieValue(name = "refresh-token") String refreshToken) {
        ReissueResponse reissueResponse = authService.reissue(accessToken, refreshToken);
        return ResponseEntity.ok().headers(reissueResponse.getHttpHeaders())
            .body(ApiResponse.createSuccessWithNoData());
    }

    @PostMapping("/auth-code")
    public ResponseEntity<ApiResponse<Object>> authCode(@Valid @RequestBody EmailRequest email) {
        emailService.sendEmail(email);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Object>> verifyCode(
        @Valid @RequestBody VerifyCodeRequest verifyCodeRequest) {
        return emailService.verifyCode(verifyCodeRequest);
    }

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<Object>> socialLogin(
        @CookieValue(name = "device-token", required = false) String deviceTokenCookie,
        @RequestBody SocialLoginRequest socialLoginRequest
    ) {

        LoginResponse loginResponse = authService.socialLogin(deviceTokenCookie,
            socialLoginRequest);

        // 소셜 로그인 성공
        if (loginResponse.getMemberResponse() != null) {
            return ResponseEntity.ok()
                .headers(loginResponse.getHttpHeaders())
                .body(ApiResponse.createSuccess(loginResponse.getMemberResponse()));
        }

        // 소셜 로그인 실패
        else {
            return ResponseEntity.ok()
                .body(ApiResponse.createError(loginResponse.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
        @CookieValue(name = "device-token", required = false) String deviceTokenCookie,
        @RequestHeader("Authorization") String accessToken,
        @CookieValue(name = "refresh-token") String refreshToken
    ) {
        LogoutResponse logoutResponse = authService.logout(deviceTokenCookie, accessToken, refreshToken);
        return ResponseEntity.ok().headers(logoutResponse.getHttpHeaders())
            .body(ApiResponse.createSuccessWithNoData());
    }
}
