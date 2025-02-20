package org.programmers.signalbuddyfinal.domain.like.batch;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class LikeJobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job likeRequestJob;

    @InjectMocks
    private LikeJobScheduler likeJobScheduler;

    @DisplayName("likeRequestJob을 잘 주입해서 실행하는지 확인한다.")
    @Test
    void runJob() throws Exception {
        likeJobScheduler.runJob();
        verify(jobLauncher, times(1))
            .run(likeRequestJob, new JobParametersBuilder().toJobParameters());
    }
}