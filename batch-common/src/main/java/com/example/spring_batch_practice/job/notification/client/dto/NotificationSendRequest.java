package com.example.spring_batch_practice.job.notification.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationSendRequest {
    private String userId;
    private String channel;
    private String message;
}
