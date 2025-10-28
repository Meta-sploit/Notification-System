package com.taskmanagement.service;

import com.taskmanagement.event.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    @Value("${app.notification.kafka.topic.notifications:notifications}")
    private String notificationTopic;

    @Value("${app.notification.enabled:true}")
    private boolean notificationsEnabled;

    @Async
    public void sendNotification(NotificationMessage notification) {
        if (!notificationsEnabled) {
            log.info("Notifications are disabled");
            return;
        }

        log.info("Sending notification: {} to {}", notification.getType(), notification.getRecipient());

        // Send to Kafka for processing
        try {
            kafkaTemplate.send(notificationTopic, notification);
            log.info("Notification sent to Kafka topic: {}", notificationTopic);
        } catch (Exception e) {
            log.error("Failed to send notification to Kafka: {}", e.getMessage(), e);
        }

        // Also send email directly
        sendEmail(notification);
    }

    @Async
    public void sendEmail(NotificationMessage notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());

            mailSender.send(message);
            log.info("Email sent successfully to: {}", notification.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", notification.getRecipient(), e.getMessage(), e);
        }
    }
}

