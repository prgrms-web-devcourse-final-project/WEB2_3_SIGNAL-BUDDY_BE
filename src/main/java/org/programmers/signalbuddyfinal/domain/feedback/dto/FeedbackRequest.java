package org.programmers.signalbuddyfinal.domain.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackRequest {

    @NotBlank(message = "피드백 제목은 비어있을 수 없습니다.")
    private String subject;

    @NotBlank(message = "피드백 내용은 비어있을 수 없습니다.")
    private String content;

    @NotNull(message = "피드백 유형은 비어있을 수 없습니다.")
    private FeedbackCategory category;

    @NotNull(message = "피드백 비밀 여부는 비어있을 수 없습니다.")
    private Boolean secret;

    @NotNull(message = "교차로 ID는 비어있을 수 없습니다.")
    private Long crossroadId;
}
