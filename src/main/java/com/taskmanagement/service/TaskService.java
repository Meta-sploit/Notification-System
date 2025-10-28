package com.taskmanagement.service;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.event.TaskEvent;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.model.AuditLog;
import com.taskmanagement.model.Task;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        log.info("Creating task: {}", taskDTO.getTitle());

        Task task = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(taskDTO.getStatus() != null ? taskDTO.getStatus() : Task.TaskStatus.TODO)
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : Task.TaskPriority.MEDIUM)
                .dueDate(taskDTO.getDueDate())
                .estimatedHours(taskDTO.getEstimatedHours())
                .tags(taskDTO.getTags())
                .reminderSent(false)
                .build();

        // Set assignee if provided
        if (taskDTO.getAssigneeId() != null) {
            User assignee = userService.getUserEntityById(taskDTO.getAssigneeId());
            task.setAssignee(assignee);
        }

        // Set creator if provided
        if (taskDTO.getCreatedById() != null) {
            User creator = userService.getUserEntityById(taskDTO.getCreatedById());
            task.setCreatedBy(creator);
        }

        Task savedTask = taskRepository.save(task);

        // Audit log
        auditLogService.log("TASK", savedTask.getId(), AuditLog.AuditAction.CREATE,
                "SYSTEM", null, savedTask.getTitle(), "Task created");

        // Publish event for real-time notification
        if (savedTask.getAssignee() != null) {
            eventPublisher.publishEvent(new TaskEvent(this, savedTask, TaskEvent.EventType.ASSIGNED));
        }

        log.info("Task created successfully: {}", savedTask.getId());
        return convertToDTO(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return convertToDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getFilteredTasks(TaskFilterDTO filter) {
        log.info("Filtering tasks with criteria: {}", filter);
        return taskRepository.findAll(TaskSpecification.filterTasks(filter)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        log.info("Updating task: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        Task.TaskStatus oldStatus = task.getStatus();
        Long oldAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;

        // Update fields
        if (taskDTO.getTitle() != null) task.setTitle(taskDTO.getTitle());
        if (taskDTO.getDescription() != null) task.setDescription(taskDTO.getDescription());
        if (taskDTO.getPriority() != null) task.setPriority(taskDTO.getPriority());
        if (taskDTO.getDueDate() != null) task.setDueDate(taskDTO.getDueDate());
        if (taskDTO.getEstimatedHours() != null) task.setEstimatedHours(taskDTO.getEstimatedHours());
        if (taskDTO.getActualHours() != null) task.setActualHours(taskDTO.getActualHours());
        if (taskDTO.getTags() != null) task.setTags(taskDTO.getTags());

        // Update status
        if (taskDTO.getStatus() != null && !taskDTO.getStatus().equals(oldStatus)) {
            task.setStatus(taskDTO.getStatus());
            if (taskDTO.getStatus() == Task.TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            }
            // Audit log for status change
            auditLogService.log("TASK", task.getId(), AuditLog.AuditAction.STATUS_CHANGE,
                    "SYSTEM", oldStatus.toString(), taskDTO.getStatus().toString(),
                    "Task status changed");

            // Publish event for status change
            eventPublisher.publishEvent(new TaskEvent(this, task, TaskEvent.EventType.STATUS_CHANGED));
        }

        // Update assignee
        if (taskDTO.getAssigneeId() != null && !taskDTO.getAssigneeId().equals(oldAssigneeId)) {
            User newAssignee = userService.getUserEntityById(taskDTO.getAssigneeId());
            task.setAssignee(newAssignee);

            // Audit log for assignment
            auditLogService.log("TASK", task.getId(), AuditLog.AuditAction.ASSIGN,
                    "SYSTEM",
                    oldAssigneeId != null ? oldAssigneeId.toString() : "null",
                    taskDTO.getAssigneeId().toString(),
                    "Task assigned");

            // Publish event for assignment
            eventPublisher.publishEvent(new TaskEvent(this, task, TaskEvent.EventType.ASSIGNED));
        }

        Task updatedTask = taskRepository.save(task);

        // General update audit log
        auditLogService.log("TASK", updatedTask.getId(), AuditLog.AuditAction.UPDATE,
                "SYSTEM", null, null, "Task updated");

        log.info("Task updated successfully: {}", updatedTask.getId());
        return convertToDTO(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        taskRepository.delete(task);

        // Audit log
        auditLogService.log("TASK", id, AuditLog.AuditAction.DELETE,
                "SYSTEM", task.getTitle(), null, "Task deleted");

        log.info("Task deleted successfully: {}", id);
    }

    @Transactional
    public void markReminderSent(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        task.setReminderSent(true);
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksNeedingReminder(LocalDateTime dueDate) {
        return taskRepository.findTasksNeedingReminder(dueDate);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = modelMapper.map(task, TaskDTO.class);
        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeName(task.getAssignee().getUsername());
        }
        if (task.getCreatedBy() != null) {
            dto.setCreatedById(task.getCreatedBy().getId());
            dto.setCreatedByName(task.getCreatedBy().getUsername());
        }
        return dto;
    }
}

