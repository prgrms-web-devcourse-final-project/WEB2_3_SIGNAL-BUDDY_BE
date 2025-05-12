package org.programmers.signalbuddyfinal.domain.feedback.dto;

import io.jsonwebtoken.lang.Collections;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminSearchCondition;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;

@Getter
public class FeedbackSearchCondition {

    private final SearchTarget target;

    private final String keyword;

    private final AnswerStatus answerStatus;

    private final Set<FeedbackCategory> categories;

    private final Optional<AdminSearchCondition> adminSearchCondition;

    @Builder
    private FeedbackSearchCondition(
        String keyword, SearchTarget target, AnswerStatus answerStatus,
        Set<FeedbackCategory> categories, AdminSearchCondition adminSearchCondition
    ) {
        this.keyword = keyword;
        this.target = target;
        this.answerStatus = answerStatus;
        this.categories = Collections.asSet(categories);
        this.adminSearchCondition = Optional.ofNullable(adminSearchCondition);
    }
}
