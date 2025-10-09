package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findAllByProjectIdOrderByCreatedAtAsc(Long projectId);
    Optional<Task> findByProjectIdAndId(Long projectId, Long taskId);
}
