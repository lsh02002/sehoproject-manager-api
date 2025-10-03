package com.sehoprojectmanagerapi.config.mapper;

// TaskMapper.java

import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {
    public TaskResponse toTaskResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getProject().getKey(),
                t.getProject().getId(),
                t.getTitle(),
                t.getDescription(),
                t.getState().name(),
                t.getPriority() != null ? t.getPriority().name() : null,
                t.getType() != null ? t.getType().name() : null,
                t.getStoryPoints(),
                t.getAssignees() == null ? List.of() :
                        t.getAssignees().stream()
                                .map(a -> new AssigneeResponse(
                                        a.getUser().getId(),
                                        a.getUser().getName()
                                ))
                                .toList(),
                t.getSprints() == null ? List.of() :
                        t.getSprints().stream()
                                .map(s -> s.getSprint().getId())   // TaskSprint → Sprint → ID
                                .toList(),
                t.getMilestones() == null ? List.of() :
                        t.getMilestones().stream()
                                .map(tm -> tm.getMilestone().getId())
                                .toList(),
                // Tags (ManyToMany → 바로 Tag 컬렉션)
                t.getTags() == null ? List.of() :
                        t.getTags().stream().map(tag -> tag.getTag().getId()).toList(),
                // Dependencies (TaskDependency 중간 엔티티)
                t.getDependencies() == null ? List.of() :
                        t.getDependencies().stream()
                                .map(d -> d.getDependsOn().getId())
                                .toList(),
                t.getDueDate(),
                t.getCreatedAt()
        );
    }
}

