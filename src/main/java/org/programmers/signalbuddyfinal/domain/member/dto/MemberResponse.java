package org.programmers.signalbuddyfinal.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class MemberResponse {


    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "회원 이메일", example = "test@example.com")
    private String email;

    @Schema(description = "회원 닉네임", example = "TestUser")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "http://example.com/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "회원 역할", example = "USER")
    private MemberRole role;

    @Schema(description = "회원 상태", exampleClasses = MemberStatus.class)
    private MemberStatus memberStatus;
}
