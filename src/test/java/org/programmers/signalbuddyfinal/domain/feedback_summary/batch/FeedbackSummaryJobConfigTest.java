package org.programmers.signalbuddyfinal.domain.feedback_summary.batch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.global.support.BatchTest;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;

class FeedbackSummaryJobConfigTest extends BatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job feedbackSummaryJob;

    @DisplayName("피드백 데이터를 집계하는 Job을 실행한다.")
    @Test
    void feedbackSummaryJob() throws Exception {
        // when
        jobLauncherTestUtils.setJob(feedbackSummaryJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }
}