package org.programmers.signalbuddyfinal.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentRequest {

    @NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    private String content;
}
