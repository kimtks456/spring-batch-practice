package com.example.spring_batch_practice.core.error;

import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Map;

@Configuration
public class BatchErrorConfig {

    // 재정의 필요 시 같은 이름의 @Bean을 선언하면 이 기본값이 대체됨
    @Bean
    @ConditionalOnMissingBean(name = "batchRetryPolicy")
    public RetryPolicy batchRetryPolicy() {
        return new SimpleRetryPolicy(3);
    }

    @Bean
    @ConditionalOnMissingBean(name = "batchSkipPolicy")
    public SkipPolicy batchSkipPolicy() {
        return new LimitCheckingItemSkipPolicy(10, Map.of(Exception.class, true));
    }
}
