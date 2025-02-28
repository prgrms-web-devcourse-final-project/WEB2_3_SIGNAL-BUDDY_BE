package org.programmers.signalbuddyfinal.domain.feedback_summary.repository;

import java.time.LocalDate;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackSummary;
import org.programmers.signalbuddyfinal.domain.feedback_summary.exception.FeedbackSummaryErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackSummaryRepository extends
    JpaRepository<FeedbackSummary, LocalDate>, CustomFeedbackSummaryRepository {

    default FeedbackSummary findByIdOrThrow(LocalDate id) {
        return findById(id)
            .orElseThrow(() -> new BusinessException(FeedbackSummaryErrorCode.NOT_FOUND_FEEDBACK_SUMMARY));
    }
}
