package com.example.spring_batch_practice.job.sample;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.sample.domain.Order;
import com.example.spring_batch_practice.job.sample.mybatis.OrderMapper;
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
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SampleMyBatisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SqlSessionFactory sqlSessionFactory;

    @Bean
    public Job sampleMyBatisJob(Step sampleMyBatisStep, JobLoggingListener jobLoggingListener) {
        return new JobBuilder("sampleMyBatisJob", jobRepository)
                .listener(jobLoggingListener)
                .start(sampleMyBatisStep)
                .build();
    }

    @Bean
    public Step sampleMyBatisStep(RetryPolicy batchRetryPolicy, SkipPolicy batchSkipPolicy) {
        return new StepBuilder("sampleMyBatisStep", jobRepository)
                .<Order, Order>chunk(100, transactionManager)
                .reader(myBatisOrderReader())
                .processor(myBatisOrderProcessor())
                .writer(myBatisOrderWriter())
                .faultTolerant()
                .retryPolicy(batchRetryPolicy)
                .skipPolicy(batchSkipPolicy)
                .build();
    }

    @Bean
    public MyBatisCursorItemReader<Order> myBatisOrderReader() {
        return new MyBatisCursorItemReaderBuilder<Order>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId(OrderMapper.class.getName() + ".selectPendingHighValueOrders")
                .build();
    }

    @Bean
    public ItemProcessor<Order, Order> myBatisOrderProcessor() {
        return order -> {
            order.setStatus("COMPLETED");
            order.setUpdatedAt(LocalDateTime.now());
            return order;
        };
    }

    @Bean
    public MyBatisBatchItemWriter<Order> myBatisOrderWriter() {
        return new MyBatisBatchItemWriterBuilder<Order>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId(OrderMapper.class.getName() + ".updateToCompleted")
                .build();
    }
}
