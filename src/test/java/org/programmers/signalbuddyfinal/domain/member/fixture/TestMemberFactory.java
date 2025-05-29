package org.programmers.signalbuddyfinal.domain.member.fixture;

import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;

public class TestMemberFactory {

    public static Member createActiveUser(Long id, String email, String nickname) {
        return Member.builder()
            .memberId(id).email(email).password("123456").role(MemberRole.USER)
            .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
    }

    public static Member createActiveUser(String email, String nickname) {
        return Member.builder()
            .email(email).password("123456").role(MemberRole.USER)
            .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
    }

    public static Member createWithdrawalUser(Long id, String email, String nickname) {
        return Member.builder()
            .memberId(id).email(email).password("123456").role(MemberRole.USER)
            .nickname(nickname).memberStatus(MemberStatus.WITHDRAWAL)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
    }

    public static Member createWithdrawalUser(String email, String nickname) {
        return Member.builder()
            .email(email).password("123456").role(MemberRole.USER)
            .nickname(nickname).memberStatus(MemberStatus.WITHDRAWAL)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
    }

    public static Member createAdmin(Long id, String email, String nickname) {
        return Member.builder()
            .memberId(id).email(email).password("123456").role(MemberRole.ADMIN)
            .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
    }

    public static Member createAdmin(String email, String nickname) {
        return Member.builder()
            .email(email).password("123456").role(MemberRole.ADMIN)
            .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
    }
}
