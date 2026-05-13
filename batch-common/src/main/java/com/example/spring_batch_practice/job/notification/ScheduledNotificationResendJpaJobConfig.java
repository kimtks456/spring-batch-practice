package com.example.spring_batch_practice.job.notification;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.notification.domain.NotificationLog;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ScheduledNotificationResendJpaJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final NotificationResendProcessor notificationResendProcessor;

    @Bean
    public Job scheduledNotificationResendJpaJob(Step scheduledNotificationResendJpaStep,
                                                 JobLoggingListener jobLoggingListener) {
        return new JobBuilder("scheduledNotificationResendJpaJob", jobRepository)
                .listener(jobLoggingListener)
                .start(scheduledNotificationResendJpaStep)
                .build();
    }

    @Bean
    public Step scheduledNotificationResendJpaStep() {
        return new StepBuilder("scheduledNotificationResendJpaStep", jobRepository)
                .<NotificationLog, NotificationLog>chunk(10, transactionManager)
                .reader(scheduledNotificationResendJpaReader())
                .processor(notificationResendProcessor)
                .writer(scheduledNotificationResendJpaWriter())
                .faultTolerant()
                .retry(Exception.class).retryLimit(1)
                .skip(Exception.class).skipLimit(50)
                .build();
    }

    @Bean
    public JpaCursorItemReader<NotificationLog> scheduledNotificationResendJpaReader() {
        return new JpaCursorItemReaderBuilder<NotificationLog>()
                .name("scheduledNotificationResendJpaReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT n FROM NotificationLog n WHERE n.status = 'FAILED' ORDER BY n.id ASC")
                .build();
    }

    @Bean
    public JpaItemWriter<NotificationLog> scheduledNotificationResendJpaWriter() {
        JpaItemWriter<NotificationLog> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        writer.setClearPersistenceContext(true);
        return writer;
    }
}
