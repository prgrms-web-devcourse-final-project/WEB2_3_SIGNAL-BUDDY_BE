package org.programmers.signalbuddyfinal.domain.postit.batch;

import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItComplete;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration("postItJobConfig")
@RequiredArgsConstructor
@Slf4j
public class JobConfig {

    private static final int chunkSize = 10;
    private final MemberRepository memberRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PostItComplete postItCompleteService;


    @Bean
    public Job completePostItJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("completePostItJob", jobRepository).incrementer(
                new RunIdIncrementer())
            .start(completePostItStep(jobRepository, transactionManager)).build();
    }

    @Bean
    @JobScope
    public Step completePostItStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("completePostItStep", jobRepository).<Postit, Postit>chunk(chunkSize,
                transactionManager)
            .allowStartIfComplete(true)
            .reader(postItCustomReader()).writer(deletedAtWriter()).build();
    }

    @Bean
    @StepScope
    public PostItCustomReader postItCustomReader() {
        return new PostItCustomReader(entityManagerFactory, chunkSize);
    }

    @Bean
    @StepScope
    public ItemWriter<Postit> deletedAtWriter() {
        LocalDateTime deletedAt = LocalDateTime.now();
        return items -> {

            if (items != null && !items.isEmpty()) {
                items.forEach(postit -> {
                    postItCompleteService.completePostIt(postit, deletedAt);
                    }
                );
            }
        };
    }
}
