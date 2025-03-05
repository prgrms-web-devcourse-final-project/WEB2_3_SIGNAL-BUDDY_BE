package org.programmers.signalbuddyfinal.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FcmTokenRequest {

    @NotBlank(message = "디바이스 토큰값은 비어있을 수 없습니다.")
    private String deviceToken;
}
