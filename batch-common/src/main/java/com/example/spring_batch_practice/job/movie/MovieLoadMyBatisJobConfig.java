package com.example.spring_batch_practice.job.movie;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.movie.client.MovieApiClient;
import com.example.spring_batch_practice.job.movie.client.dto.MovieApiDto;
import com.example.spring_batch_practice.job.movie.domain.IfMovie;
import com.example.spring_batch_practice.job.movie.mybatis.IfMovieMapper;
import com.example.spring_batch_practice.job.movie.reader.FeignMovieItemReader;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class MovieLoadMyBatisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SqlSessionFactory sqlSessionFactory;
    private final MovieApiClient movieApiClient;

    @Bean
    public Job movieLoadMyBatisJob(Step movieLoadMyBatisStep, JobLoggingListener jobLoggingListener) {
        return new JobBuilder("movieLoadMyBatisJob", jobRepository)
                .listener(jobLoggingListener)
                .start(movieLoadMyBatisStep)
                .build();
    }

    @Bean
    public Step movieLoadMyBatisStep() {
        return new StepBuilder("movieLoadMyBatisStep", jobRepository)
                .<MovieApiDto, IfMovie>chunk(5, transactionManager)
                .reader(movieLoadMyBatisReader())
                .processor(movieLoadMyBatisProcessor())
                .writer(movieLoadMyBatisWriter())
                .faultTolerant()
                .retry(Exception.class).retryLimit(3)
                .skip(Exception.class).skipLimit(20)
                .build();
    }

    @Bean
    @StepScope
    public FeignMovieItemReader movieLoadMyBatisReader() {
        return new FeignMovieItemReader(movieApiClient);
    }

    @Bean
    public ItemProcessor<MovieApiDto, IfMovie> movieLoadMyBatisProcessor() {
        return dto -> {
            IfMovie movie = IfMovie.from(dto);
            movie.setUpdatedAt(LocalDateTime.now());
            return movie;
        };
    }

    @Bean
    public MyBatisBatchItemWriter<IfMovie> movieLoadMyBatisWriter() {
        return new MyBatisBatchItemWriterBuilder<IfMovie>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId(IfMovieMapper.class.getName() + ".upsert")
                .build();
    }
}
