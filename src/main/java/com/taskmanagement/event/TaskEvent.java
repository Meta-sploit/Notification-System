package com.taskmanagement.event;

import com.taskmanagement.model.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskEvent extends ApplicationEvent {

    private final Task task;
    private final EventType eventType;

    public TaskEvent(Object source, Task task, EventType eventType) {
        super(source);
        this.task = task;
        this.eventType = eventType;
    }

    public enum EventType {
        CREATED,
        UPDATED,
        STATUS_CHANGED,
        ASSIGNED,
        DELETED,
        REMINDER
    }
}

