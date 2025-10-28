package com.taskmanagement.dto;

import com.taskmanagement.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Status is required")
    private Task.TaskStatus status;

    @NotNull(message = "Priority is required")
    private Task.TaskPriority priority;

    private LocalDateTime dueDate;

    private Long assigneeId;

    private String assigneeName;

    private Long createdById;

    private String createdByName;

    private Boolean reminderSent;

    private Integer estimatedHours;

    private Integer actualHours;

    private String tags;

    private List<FileAttachmentDTO> attachments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;
}

