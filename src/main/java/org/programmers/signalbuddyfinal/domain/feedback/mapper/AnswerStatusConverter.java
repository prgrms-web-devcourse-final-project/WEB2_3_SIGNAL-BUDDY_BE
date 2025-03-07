package org.programmers.signalbuddyfinal.domain.feedback.mapper;

import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AnswerStatusConverter implements Converter<String, AnswerStatus> {

    @Override
    public AnswerStatus convert(String source) {
        for (AnswerStatus status : AnswerStatus.values()) {
            if (status.getValue().equals(source)) {
                return status;
            }
        }
        throw new BusinessException(FeedbackErrorCode.ANSWER_STATUS_BAD_REQUEST);
    }
}
