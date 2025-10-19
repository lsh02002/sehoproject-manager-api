package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);

    List<Task> findAllByProjectIdOrderByCreatedAtAsc(Long projectId);

    Optional<Task> findByProjectIdAndId(Long projectId, Long taskId);

    List<Task> findAllByIdInAndProjectId(List<Long> ids, Long projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Task t set t.milestone = null where t.milestone.id = :milestoneId")
    void detachTasksFromMilestone(@Param("milestoneId") Long milestoneId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Task t set t.sprint = null where t.sprint.id = :sprintId")
    void detachTasksFromSprint(@Param("sprintId") Long sprintId);
}
