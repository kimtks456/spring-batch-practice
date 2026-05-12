package com.example.spring_batch_practice.core.item.writer;

import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.apache.ibatis.session.SqlSessionFactory;

public class MyBatisBatchWriterFactory {

    private MyBatisBatchWriterFactory() {}

    public static <T> MyBatisBatchItemWriter<T> create(
            SqlSessionFactory sqlSessionFactory,
            String statementId) {
        return new MyBatisBatchItemWriterBuilder<T>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId(statementId)
                .build();
    }
}
