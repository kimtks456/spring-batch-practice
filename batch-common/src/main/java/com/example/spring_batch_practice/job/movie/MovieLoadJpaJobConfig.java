package com.example.spring_batch_practice.job.movie;

import com.example.spring_batch_practice.core.listener.JobLoggingListener;
import com.example.spring_batch_practice.job.movie.client.MovieApiClient;
import com.example.spring_batch_practice.job.movie.client.dto.MovieApiDto;
import com.example.spring_batch_practice.job.movie.domain.IfMovie;
import com.example.spring_batch_practice.job.movie.reader.FeignMovieItemReader;
import com.example.spring_batch_practice.job.movie.repository.IfMovieJpaRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class MovieLoadJpaJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final MovieApiClient movieApiClient;
    private final IfMovieJpaRepository ifMovieRepository;

    @Bean
    public Job movieLoadJpaJob(Step movieLoadJpaStep, JobLoggingListener jobLoggingListener) {
        return new JobBuilder("movieLoadJpaJob", jobRepository)
                .listener(jobLoggingListener)
                .start(movieLoadJpaStep)
                .build();
    }

    @Bean
    public Step movieLoadJpaStep() {
        return new StepBuilder("movieLoadJpaStep", jobRepository)
                .<MovieApiDto, IfMovie>chunk(5, transactionManager)
                .reader(movieLoadJpaReader())
                .processor(movieLoadJpaProcessor())
                .writer(movieLoadJpaWriter())
                .faultTolerant()
                .retry(Exception.class).retryLimit(3)
                .skip(Exception.class).skipLimit(20)
                .build();
    }

    @Bean
    @StepScope
    public FeignMovieItemReader movieLoadJpaReader() {
        return new FeignMovieItemReader(movieApiClient);
    }

    @Bean
    public ItemProcessor<MovieApiDto, IfMovie> movieLoadJpaProcessor() {
        return dto -> ifMovieRepository.findByExternalId(dto.getExternalId())
                .map(existing -> {
                    existing.setTitle(dto.getTitle());
                    existing.setGenre(dto.getGenre());
                    existing.setRating(dto.getRating());
                    existing.setReleaseDate(dto.getReleaseDate());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> IfMovie.from(dto));
    }

    @Bean
    public JpaItemWriter<IfMovie> movieLoadJpaWriter() {
        JpaItemWriter<IfMovie> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        writer.setClearPersistenceContext(true);
        return writer;
    }
}
