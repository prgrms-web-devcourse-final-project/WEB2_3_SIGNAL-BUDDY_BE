package org.programmers.signalbuddyfinal.domain.feedback.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackCategory {
    ETC("기타", "etc"), DELAY("신호 지연", "delay"),
    MALFUNCTION("오작동", "malfunction"), ADD_SIGNAL("신호등 추가", "add-signal");

    private final String message;
    private final String value;

    @JsonCreator
    public static FeedbackCategory fromValue(String value) {
        for (FeedbackCategory category : FeedbackCategory.values()) {
            if (category.name().equals(value)) {
                return category;
            }
        }
        throw new BusinessException(FeedbackErrorCode.FEEDBACK_CATEGORY_BAD_REQUEST);
    }
}
