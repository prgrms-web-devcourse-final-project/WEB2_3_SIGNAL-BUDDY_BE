package org.programmers.signalbuddyfinal.domain.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmTokenRequest;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Object>> registerToken(
        @Valid @RequestBody FcmTokenRequest request,
        @CurrentUser CustomUser2Member user
    ) {
        HttpCookie cookie = fcmService.registerToken(request.getDeviceToken(), user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(ApiResponse.createSuccessWithNoData());
    }

    @PatchMapping("/token/logout")
    public ResponseEntity<ApiResponse<Object>> logoutToken(
        @CookieValue(name = "device-token", required = false) String deviceTokenUuid
    ) {
        fcmService.logoutToken(deviceTokenUuid);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @DeleteMapping("/token")
    public ResponseEntity<ApiResponse<Object>> deleteDeviceToken(
        @CookieValue(name = "device-token", required = false) String deviceTokenUuid,
        @CurrentUser CustomUser2Member user
    ) {
        HttpCookie cookie = fcmService.deleteDeviceToken(deviceTokenUuid, user);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(ApiResponse.createSuccessWithNoData());
    }
}
