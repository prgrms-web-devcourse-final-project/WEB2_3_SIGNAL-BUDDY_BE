package org.programmers.signalbuddyfinal.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;

@Entity(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus;

    // 관리자인지 확인
    public boolean isAdmin() {
        return MemberRole.ADMIN.equals(this.getRole());
    }

    // 요청자와 작성자가 다른 경우
    public static boolean isNotSameMember(CustomUser2Member user, Member member) {
        return !user.getMemberId().equals(member.getMemberId());
    }

    public void updateMember(MemberUpdateRequest request, String encodedPassword) {
        if (request.getEmail() != null) {
            this.email = request.getEmail();
        }
        if (request.getPassword() != null) {
            this.password = encodedPassword;
        }
        if (request.getNickname() != null) {
            this.nickname = request.getNickname();
        }
    }

    public void saveProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void softDelete() {
        this.memberStatus = MemberStatus.WITHDRAWAL;
    }
}
