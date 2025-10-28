package com.taskmanagement.controller;

import com.taskmanagement.model.AuditLog;
import com.taskmanagement.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "APIs for viewing audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs for a specific entity")
    public ResponseEntity<List<AuditLog>> getAuditLogsForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<AuditLog> logs = auditLogService.getAuditLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Get audit logs by user")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String username) {
        List<AuditLog> logs = auditLogService.getAuditLogsByUser(username);
        return ResponseEntity.ok(logs);
    }
}

