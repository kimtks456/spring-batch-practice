package com.example.spring_batch_practice.core.item.reader;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

public class JdbcCursorReaderFactory {

    private JdbcCursorReaderFactory() {}

    public static <T> JdbcCursorItemReader<T> create(
            DataSource dataSource,
            String sql,
            RowMapper<T> rowMapper,
            String name) {
        return new JdbcCursorItemReaderBuilder<T>()
                .name(name)
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(rowMapper)
                .build();
    }
}
