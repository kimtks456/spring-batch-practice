package com.example.spring_batch_practice.job.movie.scheduler;

import com.example.spring_batch_practice.AbstractBatchIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestPropertySource(properties = "movie.scheduler.enabled=true")
class MovieLoadSchedulerTest extends AbstractBatchIntegrationTest {

    @MockitoBean
    private JobLauncher jobLauncher;

    @Autowired
    private MovieLoadScheduler scheduler;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY = "shedlock:batch:movieLoadJob";

    @BeforeEach
    void clearLock() throws Exception {
        redisTemplate.delete(LOCK_KEY);
        given(jobLauncher.run(any(Job.class), any())).willReturn(new JobExecution(1L));
    }

    @Test
    void 성공_락_없으면_Job_실행() throws Exception {
        scheduler.run();

        verify(jobLauncher, times(1)).run(any(Job.class), any());
    }

    @Test
    void 성공_lockAtLeastFor_동안_중복실행_방지() throws Exception {
        scheduler.run();
        scheduler.run();

        verify(jobLauncher, times(1)).run(any(Job.class), any());
    }

    @Test
    void 성공_락_Redis에_직접_삽입시_Job_미실행() throws Exception {
        String lockUntil = java.time.Instant.now().plusSeconds(30)
                .atZone(java.time.ZoneId.of("UTC"))
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        redisTemplate.opsForValue().set(LOCK_KEY, lockUntil,
                java.time.Duration.ofSeconds(30));

        scheduler.run();

        verify(jobLauncher, times(0)).run(any(Job.class), any());
    }
}
