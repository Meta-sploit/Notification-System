package com.taskmanagement.service;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.exception.FileStorageException;
import com.taskmanagement.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final TaskService taskService;
    private final AuditLogService auditLogService;

    @Value("${app.csv.max-rows:10000}")
    private int maxRows;

    @Value("${app.csv.batch-size:100}")
    private int batchSize;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<TaskDTO> importTasksFromCsv(MultipartFile file) {
        log.info("Starting CSV import from file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new FileStorageException("Cannot import from empty CSV file");
        }

        List<TaskDTO> importedTasks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            List<CSVRecord> records = csvParser.getRecords();

            if (records.size() > maxRows) {
                throw new FileStorageException("CSV file exceeds maximum allowed rows: " + maxRows);
            }

            log.info("Processing {} records from CSV", records.size());

            int processedCount = 0;
            for (CSVRecord record : records) {
                try {
                    TaskDTO taskDTO = parseTaskFromCsvRecord(record);
                    TaskDTO createdTask = taskService.createTask(taskDTO);
                    importedTasks.add(createdTask);
                    processedCount++;

                    if (processedCount % batchSize == 0) {
                        log.info("Processed {} tasks", processedCount);
                    }

                } catch (Exception e) {
                    log.error("Failed to import task from row {}: {}", record.getRecordNumber(), e.getMessage());
                    // Continue with next record
                }
            }

            // Audit log
            auditLogService.log("TASK", 0L, com.taskmanagement.model.AuditLog.AuditAction.BULK_IMPORT,
                    "SYSTEM", null, String.valueOf(importedTasks.size()),
                    "Bulk import completed: " + importedTasks.size() + " tasks imported");

            log.info("CSV import completed. Successfully imported {} tasks", importedTasks.size());

        } catch (Exception e) {
            log.error("Failed to import CSV file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to import CSV file: " + e.getMessage(), e);
        }

        return importedTasks;
    }

    private TaskDTO parseTaskFromCsvRecord(CSVRecord record) {
        TaskDTO taskDTO = new TaskDTO();

        // Required fields
        taskDTO.setTitle(record.get("title"));

        // Optional fields with defaults
        taskDTO.setDescription(getOptionalField(record, "description"));

        // Status
        String status = getOptionalField(record, "status");
        taskDTO.setStatus(status != null ? Task.TaskStatus.valueOf(status.toUpperCase()) : Task.TaskStatus.TODO);

        // Priority
        String priority = getOptionalField(record, "priority");
        taskDTO.setPriority(priority != null ? Task.TaskPriority.valueOf(priority.toUpperCase()) : Task.TaskPriority.MEDIUM);

        // Due date
        String dueDate = getOptionalField(record, "due_date");
        if (dueDate != null && !dueDate.isEmpty()) {
            try {
                taskDTO.setDueDate(LocalDateTime.parse(dueDate, DATE_FORMATTER));
            } catch (Exception e) {
                log.warn("Failed to parse due date: {}", dueDate);
            }
        }

        // Assignee ID
        String assigneeId = getOptionalField(record, "assignee_id");
        if (assigneeId != null && !assigneeId.isEmpty()) {
            try {
                taskDTO.setAssigneeId(Long.parseLong(assigneeId));
            } catch (NumberFormatException e) {
                log.warn("Invalid assignee ID: {}", assigneeId);
            }
        }

        // Estimated hours
        String estimatedHours = getOptionalField(record, "estimated_hours");
        if (estimatedHours != null && !estimatedHours.isEmpty()) {
            try {
                taskDTO.setEstimatedHours(Integer.parseInt(estimatedHours));
            } catch (NumberFormatException e) {
                log.warn("Invalid estimated hours: {}", estimatedHours);
            }
        }

        // Tags
        taskDTO.setTags(getOptionalField(record, "tags"));

        return taskDTO;
    }

    private String getOptionalField(CSVRecord record, String fieldName) {
        try {
            return record.isMapped(fieldName) ? record.get(fieldName) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

