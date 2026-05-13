package com.example.spring_batch_practice.job.notification.decider;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FromDateValidationDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        String fromDate = jobExecution.getJobParameters().getString("fromDate");
        if (fromDate == null || LocalDate.parse(fromDate).isAfter(LocalDate.now())) {
            jobExecution.setExitStatus(new ExitStatus("NO_TARGET", "잘못된 fromDate 입니다."));
            return new FlowExecutionStatus("NO_TARGET");
        }
        return new FlowExecutionStatus("PROCEED");
    }
}
