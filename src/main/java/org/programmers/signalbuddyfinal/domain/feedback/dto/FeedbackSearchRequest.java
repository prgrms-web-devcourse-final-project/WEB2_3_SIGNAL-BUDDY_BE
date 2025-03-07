package org.programmers.signalbuddyfinal.domain.feedback.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;

@Getter
@AllArgsConstructor
public class FeedbackSearchRequest {

    private final String keyword;

    private final AnswerStatus status;

    private final Set<FeedbackCategory> category;
}
