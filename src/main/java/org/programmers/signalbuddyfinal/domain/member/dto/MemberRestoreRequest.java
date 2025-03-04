package org.programmers.signalbuddyfinal.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRestoreRequest {

    private String email;
}
