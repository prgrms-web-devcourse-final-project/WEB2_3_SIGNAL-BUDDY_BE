package org.programmers.signalbuddyfinal.domain.feedback.repository;

import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchCondition;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomFeedbackRepository {

    Page<FeedbackResponse> findAllByActiveMembers(
        Pageable pageable, Long crossroadId,
        FeedbackSearchCondition condition
    );

    Page<FeedbackResponse> findPagedExcludingMember(Long memberId, Pageable pageable);

    Page<FeedbackResponse> findAllByFilter(
        Pageable pageable,
        FeedbackSearchCondition condition
    );

    Feedback findByIdOrThrow(Long id);
}
