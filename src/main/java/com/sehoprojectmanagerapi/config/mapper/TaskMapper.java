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
        return TaskResponse.builder()
                .id(t.getId())
                .projectKey(t.getProject().getKey())
                .projectId(t.getProject().getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .state(t.getState().name())
                .priority(t.getPriority().name())
                .type(t.getType().name())
                .storyPoints(t.getStoryPoints())
                .assignees(
                        t.getAssignees().stream()
                                .map(a -> AssigneeResponse.builder()
                                        .userId(a.getUser().getId())
                                        .username(a.getUser().getName())
                                        .build()
                                ).toList()
                )
                .sprintId(t.getSprint() == null ? null : t.getSprint().getId())
                .milestoneId(t.getMilestone() == null ? null : t.getMilestone().getId())
                .tagIds(t.getTags().stream().map(tt -> tt.getTag().getId()).toList())
                .dependencyIds(t.getDependencies().stream().map(d -> d.getDependsOn().getId()).toList())
                .dueDate(t.getDueDate())
                .createdAt(t.getCreatedAt())
                .build();

    }
}

