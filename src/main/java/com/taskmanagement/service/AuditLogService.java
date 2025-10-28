package com.taskmanagement.service;

import com.taskmanagement.model.AuditLog;
import com.taskmanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void log(String entityType, Long entityId, AuditLog.AuditAction action,
                    String performedBy, String oldValue, String newValue, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .performedBy(performedBy)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .details(details)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} {} for {} ID: {}", action, entityType, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByUser(String username) {
        return auditLogRepository.findByPerformedBy(username);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}

