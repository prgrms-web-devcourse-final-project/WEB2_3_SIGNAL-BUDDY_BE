package org.programmers.signalbuddyfinal.domain.feedback_report.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;

@Getter
@AllArgsConstructor
public class FeedbackReportSearchRequest {

    private final String keyword;

    private final Set<FeedbackReportCategory> category;

    private final Set<FeedbackReportStatus> status;
}
