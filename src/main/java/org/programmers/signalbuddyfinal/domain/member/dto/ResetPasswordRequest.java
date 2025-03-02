package org.programmers.signalbuddyfinal.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResetPasswordRequest {

    private String email;
    private String newPassword;
}
