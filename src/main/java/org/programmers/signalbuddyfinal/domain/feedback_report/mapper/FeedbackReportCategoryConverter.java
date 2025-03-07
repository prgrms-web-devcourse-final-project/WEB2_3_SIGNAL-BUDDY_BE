package org.programmers.signalbuddyfinal.domain.feedback_report.mapper;

import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FeedbackReportCategoryConverter implements Converter<String, FeedbackReportCategory> {

    @Override
    public FeedbackReportCategory convert(String source) {
        for (FeedbackReportCategory category : FeedbackReportCategory.values()) {
            if (category.getValue().equals(source)) {
                return category;
            }
        }
        throw new BusinessException(FeedbackReportErrorCode.CATEGORY_BAD_REQUEST);
    }
}
