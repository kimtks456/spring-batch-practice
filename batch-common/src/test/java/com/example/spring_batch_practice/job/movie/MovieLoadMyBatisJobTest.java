package com.example.spring_batch_practice.job.movie;

import com.example.spring_batch_practice.AbstractBatchIntegrationTest;
import com.example.spring_batch_practice.job.movie.client.MovieApiClient;
import com.example.spring_batch_practice.job.movie.client.dto.MovieApiDto;
import com.example.spring_batch_practice.job.movie.mybatis.IfMovieMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class MovieLoadMyBatisJobTest extends AbstractBatchIntegrationTest {

    @MockitoBean
    private MovieApiClient movieApiClient;

    @Autowired private JobLauncher jobLauncher;
    @Autowired private Job movieLoadMyBatisJob;
    @Autowired private IfMovieMapper ifMovieMapper;

    private JobLauncherTestUtils launcher;

    @BeforeEach
    void setUp() {
        launcher = newLauncher();
        launcher.setJobLauncher(jobLauncher);
        launcher.setJobRepository(jobRepository);
        launcher.setJob(movieLoadMyBatisJob);

        cleanBatchMeta();
        ifMovieMapper.deleteAll();
    }

    @Test
    void 성공_신규_영화_if_movie에_적재() throws Exception {
        given(movieApiClient.getMovies(0)).willReturn(List.of(
                movie("m1", "Inception"),
                movie("m2", "Interstellar"),
                movie("m3", "Tenet")
        ));
        given(movieApiClient.getMovies(1)).willReturn(List.of());

        JobExecution execution = launch();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(ifMovieMapper.countAll()).isEqualTo(3);
    }

    @Test
    void 성공_기존_영화_업데이트_upsert() throws Exception {
        given(movieApiClient.getMovies(0)).willReturn(List.of(movie("m1", "Inception")));
        given(movieApiClient.getMovies(1)).willReturn(List.of());
        launch();

        cleanBatchMeta();
        given(movieApiClient.getMovies(0)).willReturn(List.of(movie("m1", "Inception (Updated)")));
        given(movieApiClient.getMovies(1)).willReturn(List.of());
        launch();

        assertThat(ifMovieMapper.countAll()).isEqualTo(1);
        assertThat(ifMovieMapper.findByExternalId("m1").getTitle()).isEqualTo("Inception (Updated)");
    }

    @Test
    void 성공_멀티페이지_8건_적재() throws Exception {
        given(movieApiClient.getMovies(0)).willReturn(List.of(
                movie("m1", "Movie1"), movie("m2", "Movie2"),
                movie("m3", "Movie3"), movie("m4", "Movie4"), movie("m5", "Movie5")
        ));
        given(movieApiClient.getMovies(1)).willReturn(List.of(
                movie("m6", "Movie6"), movie("m7", "Movie7"), movie("m8", "Movie8")
        ));
        given(movieApiClient.getMovies(2)).willReturn(List.of());

        JobExecution execution = launch();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(ifMovieMapper.countAll()).isEqualTo(8);
    }

    @Test
    void 성공_빈_페이지_응답시_COMPLETED() throws Exception {
        given(movieApiClient.getMovies(0)).willReturn(List.of());

        JobExecution execution = launch();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(ifMovieMapper.countAll()).isEqualTo(0);
    }

    // ─── 헬퍼 ────────────────────────────────────────────

    private JobExecution launch() throws Exception {
        return launcher.launchJob(new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters());
    }

    private MovieApiDto movie(String externalId, String title) {
        return new MovieApiDto(externalId, title, "Action",
                new BigDecimal("8.5"), LocalDate.of(2020, 1, 1));
    }
}
