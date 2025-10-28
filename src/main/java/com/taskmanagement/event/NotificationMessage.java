package com.taskmanagement.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {

    private String recipient;
    private String subject;
    private String message;
    private NotificationType type;
    private Long taskId;
    private LocalDateTime timestamp;

    public enum NotificationType {
        TASK_ASSIGNED,
        TASK_STATUS_CHANGED,
        TASK_REMINDER,
        TASK_OVERDUE
    }
}

