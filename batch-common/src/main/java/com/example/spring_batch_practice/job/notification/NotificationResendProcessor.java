package com.example.spring_batch_practice.job.notification;

import com.example.spring_batch_practice.job.notification.client.NotificationApiClient;
import com.example.spring_batch_practice.job.notification.client.dto.NotificationSendRequest;
import com.example.spring_batch_practice.job.notification.domain.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationResendProcessor implements ItemProcessor<NotificationLog, NotificationLog> {

    private final NotificationApiClient notificationApiClient;

    @Override
    public NotificationLog process(NotificationLog item) {
        try {
            notificationApiClient.send(
                    new NotificationSendRequest(item.getUserId(), item.getChannel(), item.getMessage()));
            item.setStatus("SENT");
            item.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.warn("Notification 발송 실패 id={}, channel={}", item.getId(), item.getChannel(), e);
            item.setStatus("FAILED");
        }
        item.setTrial(item.getTrial() + 1);
        return item;
    }
}
