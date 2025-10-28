package com.taskmanagement.repository;

import com.taskmanagement.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByStatus(Task.TaskStatus status);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByPriority(Task.TaskPriority priority);

    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.dueDate <= :dueDate AND t.reminderSent = false AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksNeedingReminder(@Param("dueDate") LocalDateTime dueDate);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :assigneeId AND t.status = :status")
    List<Task> findByAssigneeIdAndStatus(@Param("assigneeId") Long assigneeId, @Param("status") Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Task> searchTasks(@Param("searchTerm") String searchTerm);
}

