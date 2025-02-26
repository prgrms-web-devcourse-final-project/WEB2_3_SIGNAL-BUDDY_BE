package org.programmers.signalbuddyfinal.domain.postit.batch;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration("postItScheduler")
@RequiredArgsConstructor
public class Scheduler {

    private final JobLauncher jobLauncher;
    private final Job completePostItJob;

    @Scheduled(cron = "0 0 6,18 * * ?")
    @SchedulerLock(
        name = "DeleteMemberJobScheduler",
        lockAtMostFor = "23h",
        lockAtLeastFor = "23h")
    public void runJob() throws Exception {

        JobParameters params = new JobParametersBuilder()
            .toJobParameters();
        jobLauncher.run(completePostItJob, params);
    }
}
