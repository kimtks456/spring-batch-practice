package com.example.spring_batch_practice.core.api;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;
    private final JobRegistry jobRegistry;

    public Collection<String> getJobNames() {
        return jobRegistry.getJobNames();
    }

    public Long run(String jobName, Map<String, String> params) throws Exception {
        Job job = jobRegistry.getJob(jobName);
        JobParametersBuilder builder = new JobParametersBuilder();
        params.forEach(builder::addString);
        // 파라미터 없이 반복 실행해도 항상 새 JobInstance 생성
        if (!params.containsKey("run.id")) {
            builder.addLong("run.id", System.currentTimeMillis());
        }
        return jobLauncher.run(job, builder.toJobParameters()).getId();
    }

    public void stop(Long executionId) throws Exception {
        jobOperator.stop(executionId);
    }

    public Long restart(Long executionId) throws Exception {
        return jobOperator.restart(executionId);
    }

    public List<JobExecution> getExecutions(String jobName, int start, int count) {
        return jobExplorer.getJobInstances(jobName, start, count).stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .sorted(Comparator.comparing(JobExecution::getCreateTime).reversed())
                .collect(Collectors.toList());
    }
}
