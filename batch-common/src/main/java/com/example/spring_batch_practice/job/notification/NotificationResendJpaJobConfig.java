package com.example.spring_batch_practice.job.notification;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.notification.decider.FromDateValidationDecider;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class NotificationResendJpaJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final NotificationResendProcessor notificationResendProcessor;
    private final FromDateValidationDecider fromDateValidationDecider;

    @Bean
    public Job notificationResendJpaJob(Step notificationResendJpaStep,
                                        JobLoggingListener jobLoggingListener) {
        return new JobBuilder("notificationResendJpaJob", jobRepository)
                .listener(jobLoggingListener)
                .start(fromDateValidationDecider)
                    .on("NO_TARGET").end("NO_TARGET")
                    .on("PROCEED").to(notificationResendJpaStep)
                .end()
                .build();
    }

    @Bean
    public Step notificationResendJpaStep() {
        return new StepBuilder("notificationResendJpaStep", jobRepository)
                .<NotificationLog, NotificationLog>chunk(10, transactionManager)
                .reader(notificationResendJpaReader(null))
                .processor(notificationResendProcessor)
                .writer(notificationResendJpaWriter())
                .faultTolerant()
                .retry(Exception.class).retryLimit(3)
                .skip(Exception.class).skipLimit(10)
                .build();
    }

    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public JpaCursorItemReader<NotificationLog> notificationResendJpaReader(
            @Value("#{jobParameters['fromDate']}") String fromDate) {
        LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
        LocalDateTime to = LocalDateTime.now();
        return new JpaCursorItemReaderBuilder<NotificationLog>()
                .name("notificationResendJpaReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                        SELECT n FROM NotificationLog n
                        WHERE n.status = 'FAILED'
                          AND n.sentAt >= :from
                          AND n.sentAt <= :to
                        ORDER BY n.id ASC
                        """)
                .parameterValues(Map.of("from", from, "to", to))
                .build();
    }

    @Bean
    public JpaItemWriter<NotificationLog> notificationResendJpaWriter() {
        JpaItemWriter<NotificationLog> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        writer.setClearPersistenceContext(true);
        return writer;
    }
}
