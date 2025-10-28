package com.taskmanagement.controller;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.service.CsvImportService;
import com.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {

    private final TaskService taskService;
    private final CsvImportService csvImportService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    @Operation(summary = "Get all tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/filter")
    @Operation(summary = "Get filtered tasks")
    public ResponseEntity<List<TaskDTO>> getFilteredTasks(@RequestBody TaskFilterDTO filter) {
        List<TaskDTO> tasks = taskService.getFilteredTasks(filter);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDTO) {
        TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/csv")
    @Operation(summary = "Bulk import tasks from CSV file")
    public ResponseEntity<List<TaskDTO>> importTasksFromCsv(@RequestParam("file") MultipartFile file) {
        List<TaskDTO> importedTasks = csvImportService.importTasksFromCsv(file);
        return new ResponseEntity<>(importedTasks, HttpStatus.CREATED);
    }
}

