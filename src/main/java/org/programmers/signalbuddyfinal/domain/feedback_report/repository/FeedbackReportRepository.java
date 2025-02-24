package org.programmers.signalbuddyfinal.domain.feedback_report.repository;

import org.programmers.signalbuddyfinal.domain.feedback_report.entity.FeedbackReport;
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackReportRepository extends
    JpaRepository<FeedbackReport, Long>, CustomFeedbackReportRepository {

    default FeedbackReport findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new BusinessException(FeedbackReportErrorCode.NOT_FOUND_FEEDBACK_REPORT));
    }
}
