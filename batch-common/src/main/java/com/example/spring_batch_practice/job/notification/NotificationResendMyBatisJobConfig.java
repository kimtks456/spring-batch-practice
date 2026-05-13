package com.example.spring_batch_practice.job.notification;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.notification.decider.FromDateValidationDecider;
import com.example.spring_batch_practice.job.notification.domain.NotificationLog;
import com.example.spring_batch_practice.job.notification.mybatis.NotificationLogMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class NotificationResendMyBatisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SqlSessionFactory sqlSessionFactory;
    private final NotificationResendProcessor notificationResendProcessor;
    private final FromDateValidationDecider fromDateValidationDecider;

    @Bean
    public Job notificationResendMyBatisJob(Step notificationResendMyBatisStep,
                                            JobLoggingListener jobLoggingListener) {
        return new JobBuilder("notificationResendMyBatisJob", jobRepository)
                .listener(jobLoggingListener)
                .start(fromDateValidationDecider)
                    .on("NO_TARGET").end("NO_TARGET")
                    .on("PROCEED").to(notificationResendMyBatisStep)
                .end()
                .build();
    }

    @Bean
    public Step notificationResendMyBatisStep() {
        return new StepBuilder("notificationResendMyBatisStep", jobRepository)
                .<NotificationLog, NotificationLog>chunk(10, transactionManager)
                .reader(notificationResendMyBatisReader(null))
                .processor(notificationResendProcessor)
                .writer(notificationResendMyBatisWriter())
                .faultTolerant()
                .retry(Exception.class).retryLimit(3)
                .skip(Exception.class).skipLimit(10)
                .build();
    }

    @Bean
    @StepScope
    public MyBatisCursorItemReader<NotificationLog> notificationResendMyBatisReader(
            @Value("#{jobParameters['fromDate']}") String fromDate) {
        LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
        LocalDateTime to = LocalDateTime.now();
        MyBatisCursorItemReader<NotificationLog> reader = new MyBatisCursorItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId(NotificationLogMapper.class.getName() + ".selectFailedFrom");
        reader.setParameterValues(Map.of("from", from, "to", to));
        reader.setName("notificationResendMyBatisReader");
        return reader;
    }

    @Bean
    public MyBatisBatchItemWriter<NotificationLog> notificationResendMyBatisWriter() {
        return new MyBatisBatchItemWriterBuilder<NotificationLog>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId(NotificationLogMapper.class.getName() + ".updateStatus")
                .build();
    }
}
