package org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackReportCategory {
    INAPPROPRIATE("부적절함", "inappropriate"),
    OFFENSIVE("욕설/비방", "offensive"),
    SPAM("스팸", "spam"),
    FALSE("허위", "false"),
    ETC("기타", "etc");

    private final String message;
    private final String value;

    @JsonCreator
    public static FeedbackReportCategory fromValue(String value) {
        for (FeedbackReportCategory category : FeedbackReportCategory.values()) {
            if (category.name().equals(value)) {
                return category;
            }
        }
        throw new BusinessException(FeedbackErrorCode.FEEDBACK_CATEGORY_BAD_REQUEST);
    }
}
