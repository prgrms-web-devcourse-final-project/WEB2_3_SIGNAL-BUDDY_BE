package org.programmers.signalbuddyfinal.domain.feedback_report.repository;

import java.time.LocalDate;
import java.util.Set;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomFeedbackReportRepository {

    Page<FeedbackReportResponse> findAllByFilter(
        Pageable pageable, SearchTarget target, String keyword,
        Set<FeedbackReportCategory> categories,
        Set<FeedbackReportStatus> statuses,
        LocalDate startDate, LocalDate endDate
    );
}
