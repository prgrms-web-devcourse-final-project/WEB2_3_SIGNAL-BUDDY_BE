package org.programmers.signalbuddyfinal.domain.feedback.repository;

import java.time.LocalDate;
import java.util.Set;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomFeedbackRepository {

    Page<FeedbackResponse> findAllByActiveMembers(
        Pageable pageable,
        AnswerStatus answerStatus, Set<FeedbackCategory> categories,
        Long crossroadId, String keyword
    );

    Page<FeedbackResponse> findPagedExcludingMember(Long memberId, Pageable pageable);

    Page<FeedbackResponse> findAllByFilter(
        Pageable pageable, String keyword,
        AnswerStatus answerStatus,
        Set<FeedbackCategory> categories,
        LocalDate startDate, LocalDate endDate,
        Boolean deleted
    );

    Feedback findByIdOrThrow(Long id);
}
