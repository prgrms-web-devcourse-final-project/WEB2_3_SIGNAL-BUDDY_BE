package org.programmers.signalbuddyfinal.domain.feedback_summary.batch;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.CrossroadFeedbackCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackCategoryCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackSummary;
import org.programmers.signalbuddyfinal.domain.feedback_summary.repository.FeedbackSummaryRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FeedbackSummaryTasklet implements Tasklet {

    private final FeedbackSummaryRepository summaryRepository;

    @Override
    @Transactional
    public RepeatStatus execute(
        StepContribution contribution, ChunkContext chunkContext
    ) throws Exception {

        LocalDate now = LocalDate.now();
        Optional<FeedbackSummary> summaryEntity = summaryRepository.findById(now);
        Long todayCount = summaryRepository.countFeedbackByDate(now);
        List<FeedbackCategoryCount> categoryRanks = summaryRepository.countFeedbackCategoryByDate(now);
        List<CrossroadFeedbackCount> crossroadRanks = summaryRepository.countFeedbackOnCrossroadByDate(now);

        if (summaryEntity.isEmpty()) {
            FeedbackSummary feedbackSummary = FeedbackSummary.create()
                .todayCount(todayCount)
                .categoryRanks(categoryRanks).crossroadRanks(crossroadRanks)
                .build();
            summaryRepository.save(feedbackSummary);
        } else {
            FeedbackSummary feedbackSummary = summaryEntity.get();
            feedbackSummary.updateTodayCount(todayCount);
            feedbackSummary.updateCategoryRanks(categoryRanks);
            feedbackSummary.updateCrossroadRanks(crossroadRanks);
        }

        return RepeatStatus.FINISHED;
    }
}
