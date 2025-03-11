package org.programmers.signalbuddyfinal.domain.auth.controller;

import com.google.common.util.concurrent.UncheckedExecutionException;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.dto.EmailRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.VerifyCodeRequest;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.domain.auth.service.AuthService;
import org.programmers.signalbuddyfinal.domain.auth.service.EmailService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.global.config.AsyncConfig;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.programmers.signalbuddyfinal.global.exception.advice.dto.ErrorResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Object>> reissue(
        @CookieValue(name = "refresh-token") String refreshToken,
        @RequestHeader("Authorization") String accessToken) {
        return authService.reissue(refreshToken, accessToken);
    }

    @PostMapping("/auth-code")
    public CompletableFuture<ResponseEntity<ApiResponse<Object>>> authCode(@Valid @RequestBody EmailRequest email) {

       return emailService.sendEmail(email);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Object>> verifyCode(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest) {
        return emailService.verifyCode(verifyCodeRequest);
    }

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<?>> socialLogin(@RequestBody SocialLoginRequest socialLoginRequest){
        return authService.socialLogin(socialLoginRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
        @CookieValue(name = "refresh-token") String refreshToken,
        @RequestHeader("Authorization") String accessToken) {
        return authService.logout(refreshToken, accessToken);
    }
}
