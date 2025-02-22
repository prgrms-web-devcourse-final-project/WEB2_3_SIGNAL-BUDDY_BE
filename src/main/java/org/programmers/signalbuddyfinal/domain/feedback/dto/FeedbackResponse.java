package org.programmers.signalbuddyfinal.domain.feedback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class FeedbackResponse {

    private Long feedbackId;

    private String subject;

    private String content;

    private FeedbackCategory category;

    private Long likeCount;

    private Boolean secret;

    private AnswerStatus answerStatus;

    private String imageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private MemberResponse member;

    private CrossroadResponse crossroad;
}
