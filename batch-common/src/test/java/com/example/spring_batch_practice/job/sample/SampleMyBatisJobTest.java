package com.example.spring_batch_practice.job.sample;

import com.example.spring_batch_practice.job.sample.domain.Order;
import com.example.spring_batch_practice.job.sample.mybatis.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/sql/test-orders-schema.sql")
class SampleMyBatisJobTest {

    @Autowired private org.springframework.batch.core.launch.JobLauncher jobLauncher;
    @Autowired private org.springframework.batch.core.repository.JobRepository jobRepository;
    @Autowired private Job sampleMyBatisJob;
    @Autowired private OrderMapper orderMapper;

    private JobLauncherTestUtils launcher;

    @BeforeEach
    void setUp() {
        launcher = new JobLauncherTestUtils();
        launcher.setJobLauncher(jobLauncher);
        launcher.setJobRepository(jobRepository);
        launcher.setJob(sampleMyBatisJob);

        new JobRepositoryTestUtils(jobRepository).removeJobExecutions();
        orderMapper.deleteAll();
    }

    // ─── 성공 케이스 ─────────────────────────────────────

    @Test
    void 성공_amount_10000_초과_PENDING만_COMPLETED_처리() throws Exception {
        insertOrder("High1", "20000");
        insertOrder("High2", "50000");
        insertOrder("Low1",   "5000");
        insertOrder("Low2",  "10000");  // amount > 10000 아님 (경계값)

        JobExecution execution = launch();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(orderMapper.countByStatus("COMPLETED")).isEqualTo(2);
        assertThat(orderMapper.countByStatus("PENDING")).isEqualTo(2);
    }

    @Test
    void 성공_처리_대상_없으면_readCount_writeCount_0() throws Exception {
        insertOrder("Low1", "3000");
        insertOrder("Low2", "9999");

        JobExecution execution = launch();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        var step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(0);
        assertThat(step.getWriteCount()).isEqualTo(0);
    }

    @Test
    void 성공_readCount_writeCount가_처리건수와_일치() throws Exception {
        insertOrder("H1", "11000");
        insertOrder("H2", "22000");
        insertOrder("H3", "33000");
        insertOrder("L1",  "1000");
        insertOrder("L2",  "2000");

        JobExecution execution = launch();

        var step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(3);
        assertThat(step.getWriteCount()).isEqualTo(3);
    }

    @Test
    void 성공_빈_테이블이어도_COMPLETED() throws Exception {
        assertThat(launch().getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    // ─── 헬퍼 ────────────────────────────────────────────

    private JobExecution launch() throws Exception {
        return launcher.launchJob(
                new JobParametersBuilder()
                        .addLong("run.id", System.currentTimeMillis())
                        .toJobParameters());
    }

    private void insertOrder(String customerName, String amount) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setAmount(new BigDecimal(amount));
        order.setStatus("PENDING");
        orderMapper.insert(order);
    }
}
