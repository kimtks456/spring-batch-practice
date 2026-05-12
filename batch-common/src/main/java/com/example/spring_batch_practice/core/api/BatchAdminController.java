package com.example.spring_batch_practice.core.api;

import com.example.spring_batch_practice.core.api.dto.JobExecutionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/batch")
@RequiredArgsConstructor
public class BatchAdminController {

    private final BatchJobService batchJobService;

    @GetMapping("/jobs")
    public ResponseEntity<Collection<String>> getJobNames() {
        return ResponseEntity.ok(batchJobService.getJobNames());
    }

    @PostMapping("/jobs/{jobName}/run")
    public ResponseEntity<Long> run(
            @PathVariable String jobName,
            @RequestBody(required = false) Map<String, String> params) throws Exception {
        Long executionId = batchJobService.run(jobName, params != null ? params : Map.of());
        return ResponseEntity.ok(executionId);
    }

    @PostMapping("/executions/{executionId}/stop")
    public ResponseEntity<Void> stop(@PathVariable Long executionId) throws Exception {
        batchJobService.stop(executionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/executions/{executionId}/restart")
    public ResponseEntity<Long> restart(@PathVariable Long executionId) throws Exception {
        return ResponseEntity.ok(batchJobService.restart(executionId));
    }

    @GetMapping("/executions")
    public ResponseEntity<List<JobExecutionResponse>> getExecutions(
            @RequestParam String jobName,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "20") int count) {
        List<JobExecutionResponse> result = batchJobService.getExecutions(jobName, start, count)
                .stream()
                .map(JobExecutionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
