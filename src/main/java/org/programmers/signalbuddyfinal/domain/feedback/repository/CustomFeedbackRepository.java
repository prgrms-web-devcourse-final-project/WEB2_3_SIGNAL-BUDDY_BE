package org.programmers.signalbuddyfinal.domain.feedback.repository;

import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface CustomFeedbackRepository {

    Page<FeedbackResponse> findAllByActiveMembers(Pageable pageable, Long answerStatus);

    Page<FeedbackResponse> findPagedExcludingMember(Long memberId, Pageable pageable);

    Page<FeedbackResponse> findAll(Pageable pageable, LocalDate startDate, LocalDate endDate, Long answerStatus);
}
