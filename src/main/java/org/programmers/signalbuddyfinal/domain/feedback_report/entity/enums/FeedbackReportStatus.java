package org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackReportStatus {

    PENDING("미처리", "pending"),
    PROCESSED("처리 완료", "processed"),
    REJECTED("신고 반려", "rejected");

    private final String message;
    private final String value;

    @JsonCreator
    public static FeedbackReportStatus fromValue(String value) {
        for (FeedbackReportStatus category : FeedbackReportStatus.values()) {
            if (category.name().equals(value)) {
                return category;
            }
        }
        throw new BusinessException(FeedbackReportErrorCode.STATUS_BAD_REQUEST);
    }
}
