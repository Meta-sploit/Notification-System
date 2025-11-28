package com.taskmanagement.service;

import com.taskmanagement.event.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notification Service - Kafka Producer
 * This service publishes notification messages to Kafka.
 * The actual notification sending (email, SMS, etc.) is handled by NotificationConsumerService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KafkaTemplate<String, NotificationMessage> notificationKafkaTemplate;

    @Value("${app.notification.kafka.topic.notifications:notifications}")
    private String notificationTopic;

    @Value("${app.notification.enabled:true}")
    private boolean notificationsEnabled;

    /**
     * Publish notification message to Kafka.
     * The message will be consumed by NotificationConsumerService which handles actual delivery.
     *
     * @param notification The notification message to publish
     */
    @Async
    public void sendNotification(NotificationMessage notification) {
        if (!notificationsEnabled) {
            log.info("Notifications are disabled");
            return;
        }

        log.info("📤 Publishing notification to Kafka: {} to {}",
                notification.getType(), notification.getRecipient());

        try {
            // Publish to Kafka - consumer will handle actual sending
            notificationKafkaTemplate.send(notificationTopic, notification)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("✅ Notification published to Kafka topic '{}': {} for task ID: {}",
                                    notificationTopic, notification.getType(), notification.getTaskId());
                        } else {
                            log.error("❌ Failed to publish notification to Kafka: {}", ex.getMessage(), ex);
                        }
                    });

        } catch (Exception e) {
            log.error("❌ Exception while publishing notification to Kafka: {}", e.getMessage(), e);
            // In production, you might want to:
            // 1. Store failed notifications in database for retry
            // 2. Send alert to monitoring system
            // 3. Fall back to direct email sending
        }
    }
}

