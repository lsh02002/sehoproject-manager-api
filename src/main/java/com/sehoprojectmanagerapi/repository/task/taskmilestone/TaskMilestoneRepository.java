package com.sehoprojectmanagerapi.repository.task.taskmilestone;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskMilestoneRepository extends JpaRepository<TaskMilestone, String> {
    void deleteByTaskIdAndMilestoneId(Long taskId, Long milestoneId);
}
