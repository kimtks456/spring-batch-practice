package com.example.spring_batch_practice.core.api.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.time.LocalDateTime;

@Getter
@Builder
public class JobExecutionResponse {

    private Long executionId;
    private String jobName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String exitCode;
    private long readCount;
    private long writeCount;
    private long skipCount;

    public static JobExecutionResponse from(JobExecution execution) {
        long read = 0, write = 0, skip = 0;
        for (StepExecution step : execution.getStepExecutions()) {
            read += step.getReadCount();
            write += step.getWriteCount();
            skip += step.getReadSkipCount() + step.getWriteSkipCount() + step.getProcessSkipCount();
        }
        return JobExecutionResponse.builder()
                .executionId(execution.getId())
                .jobName(execution.getJobInstance().getJobName())
                .status(execution.getStatus().name())
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .exitCode(execution.getExitStatus().getExitCode())
                .readCount(read)
                .writeCount(write)
                .skipCount(skip)
                .build();
    }
}
