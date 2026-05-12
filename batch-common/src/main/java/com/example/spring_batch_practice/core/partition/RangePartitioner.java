package com.example.spring_batch_practice.core.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RangePartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;
    // 내부에서 신뢰할 수 있는 테이블명/컬럼명만 전달할 것 (사용자 입력 금지)
    private final String tableName;
    private final String idColumn;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long min = Optional.ofNullable(
                jdbcTemplate.queryForObject("SELECT MIN(" + idColumn + ") FROM " + tableName, Long.class)
        ).orElse(0L);
        long max = Optional.ofNullable(
                jdbcTemplate.queryForObject("SELECT MAX(" + idColumn + ") FROM " + tableName, Long.class)
        ).orElse(0L);

        Map<String, ExecutionContext> result = new HashMap<>();

        if (min == 0 && max == 0) {
            ExecutionContext ctx = new ExecutionContext();
            ctx.putLong("minId", 0);
            ctx.putLong("maxId", 0);
            result.put("partition0", ctx);
            return result;
        }

        long targetSize = (max - min) / gridSize + 1;
        long start = min;

        for (int i = 0; i < gridSize; i++) {
            long end = Math.min(start + targetSize - 1, max);
            ExecutionContext ctx = new ExecutionContext();
            ctx.putLong("minId", start);
            ctx.putLong("maxId", end);
            result.put("partition" + i, ctx);
            start = end + 1;
            if (start > max) break;
        }

        log.debug("[Batch] RangePartitioner | table={} | min={} | max={} | gridSize={} | partitions={}",
                tableName, min, max, gridSize, result.size());
        return result;
    }
}
