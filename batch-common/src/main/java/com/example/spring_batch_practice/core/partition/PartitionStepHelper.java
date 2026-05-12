package com.example.spring_batch_practice.core.partition;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.core.task.TaskExecutor;

public class PartitionStepHelper {

    private PartitionStepHelper() {}

    public static Step build(
            String managerStepName,
            JobRepository jobRepository,
            Step workerStep,
            Partitioner partitioner,
            int gridSize,
            TaskExecutor taskExecutor) {
        return new StepBuilder(managerStepName, jobRepository)
                .partitioner(workerStep.getName(), partitioner)
                .step(workerStep)
                .gridSize(gridSize)
                .taskExecutor(taskExecutor)
                .build();
    }
}
