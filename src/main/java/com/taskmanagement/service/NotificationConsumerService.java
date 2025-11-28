package com.taskmanagement.service;

import com.taskmanagement.event.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service for processing notification messages.
 * This service consumes messages from the 'notifications' Kafka topic
 * and sends them via various channels (email, SMS, push, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Consumes notification messages from Kafka and processes them.
     * This method is called automatically when a message is published to the 'notifications' topic.
     *
     * @param notification The notification message to process
     */
    @KafkaListener(
            topics = "${app.notification.kafka.topic.notifications:notifications}",
            groupId = "notification-consumer-group",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consumeNotification(NotificationMessage notification) {
        log.info("📨 Consumed notification from Kafka: {} to {}", 
                notification.getType(), notification.getRecipient());

        try {
            // Process notification based on type
            switch (notification.getType()) {
                case TASK_ASSIGNED:
                    handleTaskAssignedNotification(notification);
                    break;
                case TASK_STATUS_CHANGED:
                    handleTaskStatusChangedNotification(notification);
                    break;
                case TASK_REMINDER:
                    handleTaskReminderNotification(notification);
                    break;
                case TASK_OVERDUE:
                    handleTaskOverdueNotification(notification);
                    break;
                default:
                    log.warn("Unknown notification type: {}", notification.getType());
            }

            log.info("✅ Notification processed successfully: {}", notification.getType());

        } catch (Exception e) {
            log.error("❌ Failed to process notification: {}", e.getMessage(), e);
            // In production, you might want to:
            // 1. Retry the message (configure retry in Kafka listener)
            // 2. Send to a dead letter queue (DLQ)
            // 3. Store failed notifications in database for manual retry
        }
    }

    /**
     * Handle task assignment notifications
     */
    private void handleTaskAssignedNotification(NotificationMessage notification) {
        log.info("Processing TASK_ASSIGNED notification for task ID: {}", notification.getTaskId());
        
        // Send email
        sendEmail(notification);
        
        // Future: Add more channels
        // sendSMS(notification);
        // sendPushNotification(notification);
        // sendSlackMessage(notification);
    }

    /**
     * Handle task status change notifications
     */
    private void handleTaskStatusChangedNotification(NotificationMessage notification) {
        log.info("Processing TASK_STATUS_CHANGED notification for task ID: {}", notification.getTaskId());
        
        // Send email
        sendEmail(notification);
        
        // Future: Add more channels
        // sendPushNotification(notification);
    }

    /**
     * Handle task reminder notifications
     */
    private void handleTaskReminderNotification(NotificationMessage notification) {
        log.info("Processing TASK_REMINDER notification for task ID: {}", notification.getTaskId());
        
        // Send email
        sendEmail(notification);
        
        // Future: Add more channels
        // sendSMS(notification);
        // sendPushNotification(notification);
    }

    /**
     * Handle task overdue notifications
     */
    private void handleTaskOverdueNotification(NotificationMessage notification) {
        log.info("Processing TASK_OVERDUE notification for task ID: {}", notification.getTaskId());
        
        // Send email with high priority
        sendEmail(notification);
        
        // Future: Add more channels with escalation
        // sendSMS(notification);
        // sendPushNotification(notification);
        // notifyManager(notification);
    }

    /**
     * Send email notification
     */
    private void sendEmail(NotificationMessage notification) {
        if (mailSender == null) {
            log.warn("⚠️ JavaMailSender is not configured. Email notification will not be sent to: {}", 
                    notification.getRecipient());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());

            mailSender.send(message);
            log.info("📧 Email sent successfully to: {}", notification.getRecipient());

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", notification.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * Future: Send SMS notification
     */
    private void sendSMS(NotificationMessage notification) {
        // TODO: Implement SMS sending using Twilio, AWS SNS, etc.
        log.info("📱 SMS notification (not implemented): {}", notification.getRecipient());
    }

    /**
     * Future: Send push notification
     */
    private void sendPushNotification(NotificationMessage notification) {
        // TODO: Implement push notifications using Firebase, OneSignal, etc.
        log.info("🔔 Push notification (not implemented): {}", notification.getRecipient());
    }

    /**
     * Future: Send Slack message
     */
    private void sendSlackMessage(NotificationMessage notification) {
        // TODO: Implement Slack integration using Slack API
        log.info("💬 Slack notification (not implemented): {}", notification.getRecipient());
    }
}

