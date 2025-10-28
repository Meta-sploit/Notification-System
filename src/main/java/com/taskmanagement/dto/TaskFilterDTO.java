package com.taskmanagement.dto;

import com.taskmanagement.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskFilterDTO {

    private Task.TaskStatus status;

    private Task.TaskPriority priority;

    private Long assigneeId;

    private LocalDateTime dueDateFrom;

    private LocalDateTime dueDateTo;

    private String searchTerm;  // Search in title and description

    private Boolean reminderSent;
}

