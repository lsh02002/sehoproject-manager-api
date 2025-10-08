package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findAllByProjectIdOrderByCreatedAtAsc(Long projectId);
}
