package org.programmers.signalbuddyfinal.domain.feedback_summary.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_summary.dto.FeedbackSummaryResponse;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackSummary;
import org.programmers.signalbuddyfinal.domain.feedback_summary.mapper.FeedbackSummaryMapper;
import org.programmers.signalbuddyfinal.domain.feedback_summary.repository.FeedbackSummaryRepository;
import org.programmers.signalbuddyfinal.global.exception.advice.GlobalExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackSummaryService {

    private final FeedbackSummaryRepository feedbackSummaryRepository;
    private final GlobalExceptionHandler globalExceptionHandler;

    public FeedbackSummaryResponse getFeedbackSummary(LocalDate date) {
        FeedbackSummary feedbackSummary = feedbackSummaryRepository.findByIdOrThrow(date);
        return FeedbackSummaryMapper.INSTANCE.toResponse(feedbackSummary);
    }
}
