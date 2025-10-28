package com.taskmanagement.service;

import com.taskmanagement.event.TaskEvent;
import com.taskmanagement.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationService {

    private final TaskService taskService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.notification.reminder.hours-before-due:24}")
    private int hoursBeforeDue;

    @Scheduled(cron = "${app.notification.reminder.cron:0 0 * * * *}")
    public void sendTaskReminders() {
        log.info("Running scheduled task reminder job");

        try {
            LocalDateTime reminderThreshold = LocalDateTime.now().plusHours(hoursBeforeDue);
            List<Task> tasksNeedingReminder = taskService.getTasksNeedingReminder(reminderThreshold);

            log.info("Found {} tasks needing reminders", tasksNeedingReminder.size());

            for (Task task : tasksNeedingReminder) {
                try {
                    // Publish reminder event
                    eventPublisher.publishEvent(new TaskEvent(this, task, TaskEvent.EventType.REMINDER));

                    // Mark reminder as sent
                    taskService.markReminderSent(task.getId());

                    log.info("Reminder sent for task ID: {}", task.getId());
                } catch (Exception e) {
                    log.error("Failed to send reminder for task ID {}: {}", task.getId(), e.getMessage(), e);
                }
            }

            log.info("Scheduled task reminder job completed");
        } catch (Exception e) {
            log.error("Error in scheduled task reminder job: {}", e.getMessage(), e);
        }
    }
}

