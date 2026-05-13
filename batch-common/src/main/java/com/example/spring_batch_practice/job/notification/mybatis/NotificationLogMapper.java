package com.example.spring_batch_practice.job.notification.mybatis;

import com.example.spring_batch_practice.job.notification.domain.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationLogMapper {

    List<NotificationLog> selectFailedFrom(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    List<NotificationLog> selectAllFailed();

    void updateStatus(NotificationLog log);

    void insert(NotificationLog log);

    void deleteAll();

    int countByStatus(@Param("status") String status);
}
