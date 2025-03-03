package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AdminMemberResponse {

    private Long memberId;

    private String email;

    private String nickname;

    private Provider oauthProvider;

    private MemberRole role;

    private MemberStatus status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

}