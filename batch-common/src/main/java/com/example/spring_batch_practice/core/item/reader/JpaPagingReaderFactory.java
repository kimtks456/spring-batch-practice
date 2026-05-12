package com.example.spring_batch_practice.core.item.reader;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;

import java.util.Map;

public class JpaPagingReaderFactory {

    private JpaPagingReaderFactory() {}

    public static <T> JpaPagingItemReader<T> create(
            EntityManagerFactory emf,
            String jpql,
            Class<T> entityClass,
            int pageSize) {
        return new JpaPagingItemReaderBuilder<T>()
                .name(entityClass.getSimpleName() + "PagingReader")
                .entityManagerFactory(emf)
                .queryString(jpql)
                .pageSize(pageSize)
                .build();
    }

    public static <T> JpaPagingItemReader<T> create(
            EntityManagerFactory emf,
            String jpql,
            Map<String, Object> params,
            Class<T> entityClass,
            int pageSize) {
        return new JpaPagingItemReaderBuilder<T>()
                .name(entityClass.getSimpleName() + "PagingReader")
                .entityManagerFactory(emf)
                .queryString(jpql)
                .parameterValues(params)
                .pageSize(pageSize)
                .build();
    }
}
