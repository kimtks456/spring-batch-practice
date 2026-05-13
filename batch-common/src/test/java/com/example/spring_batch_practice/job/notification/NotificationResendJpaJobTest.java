package com.example.spring_batch_practice.job.notification;

import com.example.spring_batch_practice.AbstractBatchIntegrationTest;
import com.example.spring_batch_practice.job.notification.client.NotificationApiClient;
import com.example.spring_batch_practice.job.notification.mybatis.NotificationLogMapper;
import com.example.spring_batch_practice.job.notification.domain.NotificationLog;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class NotificationResendJpaJobTest extends AbstractBatchIntegrationTest {

    @MockitoBean
    private NotificationApiClient notificationApiClient;

    @Autowired private JobLauncher jobLauncher;
    @Autowired private Job notificationResendJpaJob;
    @Autowired private NotificationLogMapper notificationLogMapper;

    private JobLauncherTestUtils launcher;

    @BeforeEach
    void setUp() {
        launcher = newLauncher();
        launcher.setJobLauncher(jobLauncher);
        launcher.setJobRepository(jobRepository);
        launcher.setJob(notificationResendJpaJob);

        cleanBatchMeta();
        notificationLogMapper.deleteAll();
    }

    @Test
    void 성공_FAILED_알림_전체_SENT_처리() throws Exception {
        insertFailed(15, LocalDateTime.now().minusHours(1));

        JobExecution execution = launch("2020-01-01");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(notificationLogMapper.countByStatus("SENT")).isEqualTo(15);
        assertThat(notificationLogMapper.countByStatus("FAILED")).isEqualTo(0);
    }

    @Test
    void 성공_trial_처리건수마다_증가() throws Exception {
        insertFailed(5, LocalDateTime.now().minusHours(1));

        launch("2020-01-01");

        notificationLogMapper.selectAllFailed();  // 결과 없어야 함
        // trial=1 인 SENT 건 확인: 전부 5건
        assertThat(notificationLogMapper.countByStatus("SENT")).isEqualTo(5);
    }

    @Test
    void 성공_fromDate_미래시_NO_TARGET_종료() throws Exception {
        insertFailed(3, LocalDateTime.now().minusHours(1));

        JobExecution execution = launch("2099-12-31");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("NO_TARGET");
        assertThat(notificationLogMapper.countByStatus("FAILED")).isEqualTo(3);  // 처리 안 됨
    }

    @Test
    void 성공_발송_실패시_FAILED_유지_trial_증가() throws Exception {
        doThrow(new RuntimeException("외부 API 오류")).when(notificationApiClient).send(any());
        insertFailed(5, LocalDateTime.now().minusHours(1));

        JobExecution execution = launch("2020-01-01");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(notificationLogMapper.countByStatus("FAILED")).isEqualTo(5);  // 상태 변경 없음
        assertThat(notificationLogMapper.countByStatus("SENT")).isEqualTo(0);
    }

    @Test
    void 성공_멀티청크_15건_2청크_처리() throws Exception {
        insertFailed(15, LocalDateTime.now().minusHours(1));  // chunk=10 → 2청크

        JobExecution execution = launch("2020-01-01");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        var step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(15);
        assertThat(step.getWriteCount()).isEqualTo(15);
    }

    // ─── 헬퍼 ────────────────────────────────────────────

    private JobExecution launch(String fromDate) throws Exception {
        return launcher.launchJob(new JobParametersBuilder()
                .addString("fromDate", fromDate)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters());
    }

    private void insertFailed(int count, LocalDateTime sentAt) {
        for (int i = 0; i < count; i++) {
            NotificationLog log = new NotificationLog();
            log.setUserId("user_" + i);
            log.setChannel("EMAIL");
            log.setMessage("재발송 테스트 " + i);
            log.setStatus("FAILED");
            log.setSentAt(sentAt);
            notificationLogMapper.insert(log);
        }
    }
}
