package org.programmers.signalbuddyfinal.domain.comment.dto;

import lombok.*;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse {

    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MemberResponse member;
}
