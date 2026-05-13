package com.example.spring_batch_practice.job.notification;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.notification.domain.NotificationLog;
import com.example.spring_batch_practice.job.notification.mybatis.NotificationLogMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ScheduledNotificationResendMyBatisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SqlSessionFactory sqlSessionFactory;
    private final NotificationResendProcessor notificationResendProcessor;

    @Bean
    public Job scheduledNotificationResendMyBatisJob(Step scheduledNotificationResendMyBatisStep,
                                                     JobLoggingListener jobLoggingListener) {
        return new JobBuilder("scheduledNotificationResendMyBatisJob", jobRepository)
                .listener(jobLoggingListener)
                .start(scheduledNotificationResendMyBatisStep)
                .build();
    }

    @Bean
    public Step scheduledNotificationResendMyBatisStep() {
        return new StepBuilder("scheduledNotificationResendMyBatisStep", jobRepository)
                .<NotificationLog, NotificationLog>chunk(10, transactionManager)
                .reader(scheduledNotificationResendMyBatisReader())
                .processor(notificationResendProcessor)
                .writer(scheduledNotificationResendMyBatisWriter())
                .faultTolerant()
                .retry(Exception.class).retryLimit(1)
                .skip(Exception.class).skipLimit(50)
                .build();
    }

    @Bean
    public MyBatisCursorItemReader<NotificationLog> scheduledNotificationResendMyBatisReader() {
        return new MyBatisCursorItemReaderBuilder<NotificationLog>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId(NotificationLogMapper.class.getName() + ".selectAllFailed")
                .build();
    }

    @Bean
    public MyBatisBatchItemWriter<NotificationLog> scheduledNotificationResendMyBatisWriter() {
        return new MyBatisBatchItemWriterBuilder<NotificationLog>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId(NotificationLogMapper.class.getName() + ".updateStatus")
                .build();
    }
}
