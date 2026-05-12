package com.example.spring_batch_practice.core.item.writer;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;

public class JpaItemWriterFactory {

    private JpaItemWriterFactory() {}

    public static <T> JpaItemWriter<T> create(EntityManagerFactory emf) {
        JpaItemWriter<T> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
