package com.example.spring_batch_practice.job.sample.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "BatchOrder")  // JPQL ORDER 예약어 충돌 회피
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name")
    private String customerName;

    private BigDecimal amount;
    private String status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
