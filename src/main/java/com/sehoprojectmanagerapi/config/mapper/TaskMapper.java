package com.sehoprojectmanagerapi.config.mapper;

// TaskMapper.java

import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.taskassignee.AssigneeType;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.team.TeamRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TaskMapper {
    UserRepository userRepository;
    TeamRepository teamRepository;

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
                                .map(a -> {
                                    if (a.getAssigneeType() == AssigneeType.USER) {
                                        User user = userRepository.findById(a.getAssigneeId())
                                                .orElse(null);
                                        return AssigneeResponse.builder()
                                                .userId(user != null ? user.getId() : null)
                                                .username(user != null ? user.getName() : null)
                                                .type("USER")
                                                .build();
                                    } else if (a.getAssigneeType() == AssigneeType.TEAM) {
                                        Team team = teamRepository.findById(a.getAssigneeId())
                                                .orElse(null);
                                        return AssigneeResponse.builder()
                                                .userId(null)
                                                .username(team != null ? team.getName() : null)
                                                .type("TEAM")
                                                .build();
                                    } else {
                                        return null;
                                    }
                                }).filter(Objects::nonNull)
                                .toList())
                .sprintId(t.getSprint() == null ? null : t.getSprint().getId())
                .milestoneId(t.getMilestone() == null ? null : t.getMilestone().getId())
                .tagIds(
                        t.getTags().stream()
                                .map(Tag::getId)
                                .toList()
                )
                .dependencyIds(t.getDependencies().stream().map(d -> d.getDependsOn().getId()).toList())
                .dueDate(t.getDueDate())
                .createdAt(t.getCreatedAt())
                .build();

    }
}

