package org.programmers.signalbuddyfinal.domain.feedback_report.repository;

import org.programmers.signalbuddyfinal.domain.feedback_report.entity.FeedbackReport;
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackReportRepository extends
    JpaRepository<FeedbackReport, Long>, CustomFeedbackReportRepository {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE feedback_reports r SET r.deletedAt = now() WHERE r.feedback.feedbackId = :feedbackId")
    void deleteAllByFeedbackId(@Param("feedbackId") Long feedbackId);

    default FeedbackReport findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new BusinessException(FeedbackReportErrorCode.NOT_FOUND_FEEDBACK_REPORT));
    }
}
