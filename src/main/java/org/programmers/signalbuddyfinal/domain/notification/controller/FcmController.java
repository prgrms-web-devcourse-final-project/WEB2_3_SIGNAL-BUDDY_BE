package org.programmers.signalbuddyfinal.domain.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmTokenRequest;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        fcmService.registerToken(request.getDeviceToken(), user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccessWithNoData());
    }
}
