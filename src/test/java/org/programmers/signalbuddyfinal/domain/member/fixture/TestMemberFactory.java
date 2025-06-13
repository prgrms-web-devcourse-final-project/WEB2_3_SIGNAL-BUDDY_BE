package org.programmers.signalbuddyfinal.domain.member.fixture;

import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;

public class TestMemberFactory {

    public static Member createActiveUser(Long id, String email, String nickname) {
        return baseBuilder(email, nickname, MemberRole.USER)
            .memberId(id)
            .build();
    }

    public static Member createActiveUser(String email, String nickname) {
        return baseBuilder(email, nickname, MemberRole.USER)
            .build();
    }

    public static Member createWithdrawalUser(Long id, String email, String nickname) {
        return baseBuilder(email, nickname, MemberRole.USER)
            .memberId(id)
            .memberStatus(MemberStatus.WITHDRAWAL)
            .build();
    }

    public static Member createWithdrawalUser(String email, String nickname) {
        return baseBuilder(email, nickname, MemberRole.USER)
            .memberStatus(MemberStatus.WITHDRAWAL)
            .build();
    }

    public static Member createAdmin(Long id, String email, String nickname) {
        return baseBuilder(email, nickname, MemberRole.ADMIN)
            .memberId(id)
            .build();
    }

    public static Member createAdmin(String email, String nickname) {
        return baseBuilder(email, nickname, MemberRole.ADMIN)
            .build();
    }

    private static Member.MemberBuilder baseBuilder(
        String email, String nickname, MemberRole role
    ) {
        return Member.builder()
            .email(email).password("123456").role(role)
            .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131");
    }
}
