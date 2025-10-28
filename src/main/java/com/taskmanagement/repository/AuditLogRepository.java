package com.taskmanagement.repository;

import com.taskmanagement.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByPerformedBy(String performedBy);

    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByAction(AuditLog.AuditAction action);
}

