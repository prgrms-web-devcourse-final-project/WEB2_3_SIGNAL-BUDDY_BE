package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AdminMemberRequest {

    private Long memberId;

    private String email;

    private String nickname;

    private String oauthProvider;

    private MemberRole role;

    private MemberStatus memberStatus;

    private LocalDateTime createdAt;

}