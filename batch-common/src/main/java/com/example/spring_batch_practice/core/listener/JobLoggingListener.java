package com.example.spring_batch_practice.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class JobLoggingListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[Batch] Job 시작 | name={} | params={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long durationMs = (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null)
                ? Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis()
                : -1;
        log.info("[Batch] Job 종료 | name={} | status={} | exitCode={} | duration={}ms",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                jobExecution.getExitStatus().getExitCode(),
                durationMs);
    }
}
