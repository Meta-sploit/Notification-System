package com.taskmanagement.event;

import com.taskmanagement.model.Task;
import com.taskmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * TaskEventListener - Handles task events and publishes notifications to Kafka.
 *
 * IMPORTANT: Uses @TransactionalEventListener instead of @EventListener to ensure:
 * 1. Events are only processed AFTER the database transaction commits successfully
 * 2. No notifications are sent if the transaction rolls back
 * 3. Listeners can safely read committed data from the database
 *
 * Phase: AFTER_COMMIT - Listener executes only after successful transaction commit
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final NotificationService notificationService;

    /**
     * Handle task events after transaction commits.
     *
     * @TransactionalEventListener with AFTER_COMMIT ensures:
     * - Event is processed ONLY if transaction commits successfully
     * - If transaction rolls back, this listener will NOT execute
     * - Database changes are visible to this listener
     *
     * @Async ensures non-blocking execution in a separate thread
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskEvent(TaskEvent event) {
        log.info("✅ Transaction committed - Handling task event: {} for task ID: {}",
                event.getEventType(), event.getTask().getId());

        Task task = event.getTask();

        switch (event.getEventType()) {
            case ASSIGNED:
                sendAssignmentNotification(task);
                break;
            case STATUS_CHANGED:
                sendStatusChangeNotification(task);
                break;
            case REMINDER:
                sendReminderNotification(task);
                break;
            default:
                log.debug("No notification needed for event type: {}", event.getEventType());
        }
    }

    private void sendAssignmentNotification(Task task) {
        if (task.getAssignee() == null) {
            return;
        }

        NotificationMessage notification = NotificationMessage.builder()
                .recipient(task.getAssignee().getEmail())
                .subject("New Task Assigned: " + task.getTitle())
                .message(buildAssignmentMessage(task))
                .type(NotificationMessage.NotificationType.TASK_ASSIGNED)
                .taskId(task.getId())
                .timestamp(LocalDateTime.now())
                .build();

        notificationService.sendNotification(notification);
    }

    private void sendStatusChangeNotification(Task task) {
        if (task.getAssignee() == null) {
            return;
        }

        NotificationMessage notification = NotificationMessage.builder()
                .recipient(task.getAssignee().getEmail())
                .subject("Task Status Updated: " + task.getTitle())
                .message(buildStatusChangeMessage(task))
                .type(NotificationMessage.NotificationType.TASK_STATUS_CHANGED)
                .taskId(task.getId())
                .timestamp(LocalDateTime.now())
                .build();

        notificationService.sendNotification(notification);
    }

    private void sendReminderNotification(Task task) {
        if (task.getAssignee() == null) {
            return;
        }

        NotificationMessage notification = NotificationMessage.builder()
                .recipient(task.getAssignee().getEmail())
                .subject("Task Reminder: " + task.getTitle())
                .message(buildReminderMessage(task))
                .type(NotificationMessage.NotificationType.TASK_REMINDER)
                .taskId(task.getId())
                .timestamp(LocalDateTime.now())
                .build();

        notificationService.sendNotification(notification);
    }

    private String buildAssignmentMessage(Task task) {
        return String.format(
                "Hello %s,\n\n" +
                "You have been assigned a new task:\n\n" +
                "Title: %s\n" +
                "Description: %s\n" +
                "Priority: %s\n" +
                "Due Date: %s\n\n" +
                "Please review and start working on it.\n\n" +
                "Best regards,\n" +
                "Task Management System",
                task.getAssignee().getFirstName() != null ? task.getAssignee().getFirstName() : task.getAssignee().getUsername(),
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "N/A",
                task.getPriority(),
                task.getDueDate() != null ? task.getDueDate().toString() : "N/A"
        );
    }

    private String buildStatusChangeMessage(Task task) {
        return String.format(
                "Hello %s,\n\n" +
                "The status of your task has been updated:\n\n" +
                "Title: %s\n" +
                "New Status: %s\n" +
                "Priority: %s\n\n" +
                "Best regards,\n" +
                "Task Management System",
                task.getAssignee().getFirstName() != null ? task.getAssignee().getFirstName() : task.getAssignee().getUsername(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority()
        );
    }

    private String buildReminderMessage(Task task) {
        return String.format(
                "Hello %s,\n\n" +
                "This is a reminder about your upcoming task:\n\n" +
                "Title: %s\n" +
                "Description: %s\n" +
                "Priority: %s\n" +
                "Due Date: %s\n\n" +
                "Please ensure you complete it on time.\n\n" +
                "Best regards,\n" +
                "Task Management System",
                task.getAssignee().getFirstName() != null ? task.getAssignee().getFirstName() : task.getAssignee().getUsername(),
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "N/A",
                task.getPriority(),
                task.getDueDate() != null ? task.getDueDate().toString() : "N/A"
        );
    }
}

