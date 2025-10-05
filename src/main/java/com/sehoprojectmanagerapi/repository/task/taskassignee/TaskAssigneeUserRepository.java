package com.sehoprojectmanagerapi.repository.task.taskassignee;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssigneeUserRepository extends JpaRepository<TaskAssigneeUser, Long> {
    boolean existsByTaskIdAndUserId(Long taskId, Long userId);
}
