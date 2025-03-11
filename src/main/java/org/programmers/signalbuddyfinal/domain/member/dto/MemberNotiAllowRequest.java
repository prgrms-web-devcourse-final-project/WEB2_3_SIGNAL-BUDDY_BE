package org.programmers.signalbuddyfinal.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberNotiAllowRequest {

    @NotNull(message = "알림 허용 여부는 비어있을 수 없습니다.")
    private final Boolean notifyEnabled;
}
