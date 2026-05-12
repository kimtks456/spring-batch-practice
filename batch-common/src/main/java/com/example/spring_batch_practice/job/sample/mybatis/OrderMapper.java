package com.example.spring_batch_practice.job.sample.mybatis;

import com.example.spring_batch_practice.job.sample.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    List<Order> selectPendingHighValueOrders();

    void updateToCompleted(Order order);

    // ─── 테스트 helper ────────────────────────────────────
    void insert(Order order);

    int countByStatus(@Param("status") String status);

    void deleteAll();
}
