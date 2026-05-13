package com.example.spring_batch_practice;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 싱글턴 컨테이너 패턴 — JVM 전체에서 컨테이너를 한 번만 기동.
 * @Testcontainers + @Container static 조합은 테스트 클래스별로 컨테이너를 재시작해
 * Spring 컨텍스트 캐시와 충돌하므로 사용하지 않는다.
 */
@SpringBootTest
@ActiveProfiles("testcontainers")
public abstract class AbstractBatchIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES;
    static final GenericContainer<?> REDIS;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
                .withInitScript("sql/init-testcontainers-schema.sql");
        REDIS = new GenericContainer<>("redis:7")
                .withExposedPorts(6379);
        POSTGRES.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    protected JobRepository jobRepository;

    protected JobLauncherTestUtils newLauncher() {
        return new JobLauncherTestUtils();
    }

    protected void cleanBatchMeta() {
        new JobRepositoryTestUtils(jobRepository).removeJobExecutions();
    }
}
