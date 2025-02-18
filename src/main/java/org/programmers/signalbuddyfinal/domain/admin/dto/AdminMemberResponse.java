package org.programmers.signalbuddyfinal.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AdminMemberResponse {

    private Long memberId;

    private String email;

    private String nickname;

    private String profileImageUrl;

    private MemberRole role;

    private MemberStatus memberStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String userAddress;

    private int bookmarkCount;

    private List<AdminBookmarkResponse> bookmarkResponses;

}
