package com.example.spring_batch_practice.core.item.reader;

import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Map;

public class MyBatisCursorReaderFactory {

    private MyBatisCursorReaderFactory() {}

    public static <T> MyBatisCursorItemReader<T> create(
            SqlSessionFactory sqlSessionFactory,
            String queryId) {
        return new MyBatisCursorItemReaderBuilder<T>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId(queryId)
                .build();
    }

    public static <T> MyBatisCursorItemReader<T> create(
            SqlSessionFactory sqlSessionFactory,
            String queryId,
            Map<String, Object> params) {
        return new MyBatisCursorItemReaderBuilder<T>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId(queryId)
                .parameterValues(params)
                .build();
    }
}
