package org.programmers.signalbuddyfinal.domain.feedback.repository;

import java.time.LocalDate;
import java.util.Set;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
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

    Page<FeedbackResponse> findAll(
        Pageable pageable,
        LocalDate startDate, LocalDate endDate,
        AnswerStatus answerStatus
    );
}
