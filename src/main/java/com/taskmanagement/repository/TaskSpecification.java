package com.taskmanagement.repository;

import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.model.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filterTasks(TaskFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getPriority() != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), filter.getPriority()));
            }

            if (filter.getAssigneeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), filter.getAssigneeId()));
            }

            if (filter.getDueDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            }

            if (filter.getDueDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            }

            if (filter.getReminderSent() != null) {
                predicates.add(criteriaBuilder.equal(root.get("reminderSent"), filter.getReminderSent()));
            }

            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchPattern
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern
                );
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

