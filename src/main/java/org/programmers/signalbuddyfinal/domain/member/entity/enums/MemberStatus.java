package org.programmers.signalbuddyfinal.domain.member.entity.enums;

import lombok.Getter;

@Getter
public enum MemberStatus {
    ACTIVITY("활성"), WITHDRAWAL("탈퇴");

    private final String status;

    MemberStatus(String status) {
        this.status = status;
    }
}
