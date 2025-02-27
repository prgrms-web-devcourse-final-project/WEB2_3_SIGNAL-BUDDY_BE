package org.programmers.signalbuddyfinal.domain.feedback_summary.repository;

import java.time.LocalDate;
import java.util.List;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.CrossroadFeedbackCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackCategoryCount;

public interface CustomFeedbackSummaryRepository {

    List<CrossroadFeedbackCount> countFeedbackOnCrossroadByDate(LocalDate date);

    List<FeedbackCategoryCount> countFeedbackCategoryByDate(LocalDate date);

    long countFeedbackByDate(LocalDate date);
}
