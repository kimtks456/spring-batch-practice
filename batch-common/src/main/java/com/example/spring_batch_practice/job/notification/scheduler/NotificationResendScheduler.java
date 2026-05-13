package com.example.spring_batch_practice.job.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.scheduler.enabled", havingValue = "true")
public class NotificationResendScheduler {

    private final JobLauncher jobLauncher;
    private final Job scheduledNotificationResendJpaJob;

    @Scheduled(fixedDelayString = "${notification.scheduler.fixed-delay:60000}",
               initialDelayString = "${notification.scheduler.initial-delay:0}")
    @SchedulerLock(
            name = "scheduledNotificationResendJob",
            lockAtMostFor = "PT55S",
            lockAtLeastFor = "PT5S"
    )
    public void run() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(scheduledNotificationResendJpaJob, params);
        } catch (Exception e) {
            log.error("scheduledNotificationResendJob 실행 실패", e);
        }
    }
}
