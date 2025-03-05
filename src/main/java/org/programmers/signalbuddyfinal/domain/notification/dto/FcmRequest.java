package org.programmers.signalbuddyfinal.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FcmRequest {

    @NotBlank(message = "디바이스 토큰값은 비어있을 수 없습니다.")
    private String deviceToken;

    @NotBlank(message = "알림 제목은 비어있을 수 없습니다.")
    private String title;

    @NotBlank(message = "알림 내용은 비어있을 수 없습니다.")
    private String content;
}
