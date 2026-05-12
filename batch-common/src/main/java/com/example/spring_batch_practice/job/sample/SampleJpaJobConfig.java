package com.example.spring_batch_practice.job.sample;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.sample.domain.Order;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SampleJpaJobConfig {

    private static final String JPQL =
            "SELECT o FROM BatchOrder o WHERE o.status = 'PENDING' AND o.amount > 10000";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job sampleJpaJob(Step sampleJpaStep, JobLoggingListener jobLoggingListener) {
        return new JobBuilder("sampleJpaJob", jobRepository)
                .listener(jobLoggingListener)
                .start(sampleJpaStep)
                .build();
    }

    @Bean
    public Step sampleJpaStep(RetryPolicy batchRetryPolicy, SkipPolicy batchSkipPolicy) {
        return new StepBuilder("sampleJpaStep", jobRepository)
                .<Order, Order>chunk(100, transactionManager)
                .reader(jpaOrderReader())
                .processor(jpaOrderProcessor())
                .writer(jpaOrderWriter())
                .faultTolerant()
                .retryPolicy(batchRetryPolicy)
                .skipPolicy(batchSkipPolicy)
                .build();
    }

    @Bean
    public JpaCursorItemReader<Order> jpaOrderReader() {
        return new JpaCursorItemReaderBuilder<Order>()
                .name("jpaOrderReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(JPQL)
                .build();
    }

    @Bean
    public ItemProcessor<Order, Order> jpaOrderProcessor() {
        return order -> {
            order.setStatus("COMPLETED");
            order.setUpdatedAt(LocalDateTime.now());
            return order;
        };
    }

    @Bean
    public JpaItemWriter<Order> jpaOrderWriter() {
        JpaItemWriter<Order> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        writer.setClearPersistenceContext(true);  // 청크 커밋 후 1차 캐시 정리 (대용량 배치 메모리 관리)
        return writer;
    }
}
