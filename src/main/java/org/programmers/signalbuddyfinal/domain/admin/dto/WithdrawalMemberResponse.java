package org.programmers.signalbuddyfinal.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class WithdrawalMemberResponse {
    private Long memberId;

    private String email;

    private String nickname;

    private String profileImageUrl;

    private MemberRole role;

    private MemberStatus memberStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
