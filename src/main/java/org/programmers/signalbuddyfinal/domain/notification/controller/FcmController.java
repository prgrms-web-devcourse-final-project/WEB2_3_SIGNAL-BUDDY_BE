package org.programmers.signalbuddyfinal.domain.notification.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmRequest;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/push")
    public ResponseEntity<ApiResponse<Object>> pushMessage(
        @Valid @RequestBody FcmRequest request
    ) {
        fcmService.sendMessage(request);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerToken(
        @NotBlank @RequestBody String deviceTokent
    ) {
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }
}
