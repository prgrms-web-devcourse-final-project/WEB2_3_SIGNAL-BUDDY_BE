package org.programmers.signalbuddyfinal.domain.feedback_summary.batch;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class FeedbackSummaryScheduler {

    private final JobLauncher jobLauncher;
    private final Job feedbackSummaryJob;

    @Scheduled(cron = "${schedule.feedback-summary-job.cron}")
    @SchedulerLock(
        name = "FeedbackSummaryJob",
        lockAtMostFor = "${schedule.feedback-summary-job.lockAtMostFor}",
        lockAtLeastFor = "${schedule.feedback-summary-job.lockAtLeastFor}")
    public void runJob()
        throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
        JobParametersInvalidException, JobRestartException {

        JobParameters params = new JobParametersBuilder()
            .toJobParameters();
        jobLauncher.run(feedbackSummaryJob, params);
    }
}
