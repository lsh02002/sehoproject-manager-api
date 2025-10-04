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
                t.getPriority().name(),
                t.getType().name(),
                t.getStoryPoints(),
                t.getAssignees().stream()
                        .map(a -> new AssigneeResponse(a.getUser().getId(), a.getUser().getName()))
                        .toList(),
                t.getSprint() == null ? null : t.getSprint().getId(),   // 단일 sprint
                t.getMilestone() == null ? null : t.getMilestone().getId(), // 단일 milestone
                t.getTags().stream().map(tt -> tt.getTag().getId()).toList(),
                t.getDependencies().stream().map(d -> d.getDependsOn().getId()).toList(),
                t.getDueDate(),
                t.getCreatedAt()
        );
    }
}

