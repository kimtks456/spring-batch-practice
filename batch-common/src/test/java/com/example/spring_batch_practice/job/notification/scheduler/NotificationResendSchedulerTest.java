package com.example.spring_batch_practice.job.notification.scheduler;

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

@TestPropertySource(properties = "notification.scheduler.enabled=true")
class NotificationResendSchedulerTest extends AbstractBatchIntegrationTest {

    // 실제 Job 실행 없이 ShedLock 동작만 검증
    @MockitoBean
    private JobLauncher jobLauncher;

    @Autowired
    private NotificationResendScheduler scheduler;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY = "shedlock:batch:scheduledNotificationResendJob";

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
        scheduler.run();           // 첫 실행: 락 획득 + Job 실행
        scheduler.run();           // 즉시 재실행: lockAtLeastFor(5s) 동안 락 유지 → 실행 안 됨

        verify(jobLauncher, times(1)).run(any(Job.class), any());
    }

    @Test
    void 성공_락_Redis에_직접_삽입시_Job_미실행() throws Exception {
        // Redis에 직접 락 삽입 → 스케줄러가 락 획득 실패
        String lockUntil = java.time.Instant.now().plusSeconds(30)
                .atZone(java.time.ZoneId.of("UTC"))
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        redisTemplate.opsForValue().set(LOCK_KEY, lockUntil,
                java.time.Duration.ofSeconds(30));

        scheduler.run();

        verify(jobLauncher, times(0)).run(any(Job.class), any());
    }
}
