package org.programmers.signalbuddyfinal.domain.feedback_summary.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class FeedbackSummaryJobConfig {

    private final FeedbackSummaryTasklet feedbackSummaryTasklet;

    @Bean
    public Job feedbackSummaryJob(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("feedbackSummaryJob", jobRepository)
            .incrementer(new RunIdIncrementer())    // 동일한 파라미터를 다시 실행
            .start(summaryFeedback(jobRepository, transactionManager))
            .build();
    }

    @Bean
    @JobScope
    public Step summaryFeedback(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("summaryFeedback", jobRepository)
            .tasklet(feedbackSummaryTasklet, transactionManager)
            .allowStartIfComplete(true) // COMPLETED 되어도 재실행에 포함시키기
            .build();
    }
}
