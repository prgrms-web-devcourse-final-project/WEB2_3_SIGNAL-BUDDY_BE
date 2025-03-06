package org.programmers.signalbuddyfinal.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.security.jwt.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/test")
@RequiredArgsConstructor
public class AuthTestController {

    private final JwtService jwtService;

    // 액세스 토큰 만료 -> 시간에 따른 만료
    @PostMapping("/time-expire/{memberId}")
    public ResponseEntity<ApiResponse<Object>> timeExpire(@PathVariable Long memberId) {

        return jwtService.createShortTimeAccessToken(memberId);
    }

    // 액세스 토큰 블랙리스트 추가 -> 새로운 토큰, 기존 토큰 만료
    @PostMapping("/blacklist-expire")
    public ResponseEntity<ApiResponse<Object>> blacklistExpire(@RequestParam String accessToken) {
        return jwtService.addBlackListExistingAccessTokenForTest(accessToken);
    }

    // 블랙리스트 해제
    @DeleteMapping("/blacklist-expire")
    public ResponseEntity<ApiResponse<Object>> clearBlacklist(@RequestParam String accessToken) {
        return jwtService.deleteBlackListExistingAccessTokenForTest(accessToken);
    }
}
