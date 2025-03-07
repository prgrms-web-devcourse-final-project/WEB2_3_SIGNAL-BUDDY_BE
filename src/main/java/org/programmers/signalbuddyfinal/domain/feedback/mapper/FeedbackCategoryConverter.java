package org.programmers.signalbuddyfinal.domain.feedback.mapper;

import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FeedbackCategoryConverter implements Converter<String, FeedbackCategory> {

    @Override
    public FeedbackCategory convert(String source) {
        for (FeedbackCategory category : FeedbackCategory.values()) {
            if (category.getValue().equals(source)) {
                return category;
            }
        }
        throw new BusinessException(FeedbackErrorCode.FEEDBACK_CATEGORY_BAD_REQUEST);
    }
}
