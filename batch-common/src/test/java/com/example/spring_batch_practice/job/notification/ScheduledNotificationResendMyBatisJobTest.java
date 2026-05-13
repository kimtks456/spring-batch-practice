package com.example.spring_batch_practice.job.notification;

import com.example.spring_batch_practice.AbstractBatchIntegrationTest;
import com.example.spring_batch_practice.job.notification.client.NotificationApiClient;
import com.example.spring_batch_practice.job.notification.domain.NotificationLog;
import com.example.spring_batch_practice.job.notification.mybatis.NotificationLogMapper;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledNotificationResendMyBatisJobTest extends AbstractBatchIntegrationTest {

    @MockitoBean
    private NotificationApiClient notificationApiClient;

    @Autowired private JobLauncher jobLauncher;
    @Autowired private Job scheduledNotificationResendMyBatisJob;
    @Autowired private NotificationLogMapper notificationLogMapper;

    private JobLauncherTestUtils launcher;

    @BeforeEach
    void setUp() {
        launcher = newLauncher();
        launcher.setJobLauncher(jobLauncher);
        launcher.setJobRepository(jobRepository);
        launcher.setJob(scheduledNotificationResendMyBatisJob);

        cleanBatchMeta();
        notificationLogMapper.deleteAll();
    }

    @Test
    void 성공_FAILED_알림_전체_SENT_처리() throws Exception {
        insertFailed(15);

        JobExecution execution = launch();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(notificationLogMapper.countByStatus("SENT")).isEqualTo(15);
        assertThat(notificationLogMapper.countByStatus("FAILED")).isEqualTo(0);
    }

    @Test
    void 성공_PENDING_상태는_처리_안함() throws Exception {
        insertFailed(5);
        insertPending(3);

        launch();

        assertThat(notificationLogMapper.countByStatus("SENT")).isEqualTo(5);
        assertThat(notificationLogMapper.countByStatus("PENDING")).isEqualTo(3);
    }

    @Test
    void 성공_빈_테이블이면_COMPLETED() throws Exception {
        assertThat(launch().getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    // ─── 헬퍼 ────────────────────────────────────────────

    private JobExecution launch() throws Exception {
        return launcher.launchJob(new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters());
    }

    private void insertFailed(int count) {
        for (int i = 0; i < count; i++) {
            NotificationLog log = new NotificationLog();
            log.setUserId("user_" + i);
            log.setChannel("EMAIL");
            log.setMessage("메시지 " + i);
            log.setStatus("FAILED");
            log.setSentAt(LocalDateTime.now().minusHours(1));
            notificationLogMapper.insert(log);
        }
    }

    private void insertPending(int count) {
        for (int i = 0; i < count; i++) {
            NotificationLog log = new NotificationLog();
            log.setUserId("pending_" + i);
            log.setChannel("SMS");
            log.setMessage("대기 " + i);
            log.setStatus("PENDING");
            notificationLogMapper.insert(log);
        }
    }
}
