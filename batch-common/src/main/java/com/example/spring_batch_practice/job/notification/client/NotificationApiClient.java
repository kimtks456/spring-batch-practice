package com.example.spring_batch_practice.job.notification.client;

import com.example.spring_batch_practice.job.notification.client.dto.NotificationSendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificationApi", url = "${notification.api.url}")
public interface NotificationApiClient {

    @PostMapping("/api/notifications/send")
    void send(@RequestBody NotificationSendRequest request);
}
