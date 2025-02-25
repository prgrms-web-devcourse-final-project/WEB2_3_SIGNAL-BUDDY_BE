package org.programmers.signalbuddyfinal.domain.feedback_report.mapper;

import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FeedbackReportStatusConverter implements Converter<String, FeedbackReportStatus> {

    @Override
    public FeedbackReportStatus convert(String source) {
        for (FeedbackReportStatus category : FeedbackReportStatus.values()) {
            if (category.getValue().equals(source)) {
                return category;
            }
        }
        throw new BusinessException(FeedbackReportErrorCode.STATUS_BAD_REQUEST);
    }
}
