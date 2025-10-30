package com.taskmanagement.service;

import com.taskmanagement.event.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    public NotificationService(KafkaTemplate<String, NotificationMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

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
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email notification will not be sent to: {}", notification.getRecipient());
            return;
        }

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

