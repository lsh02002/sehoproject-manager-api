package com.sehoprojectmanagerapi.web.mapper;

// TaskMapper.java

import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.taskassignee.AssigneeType;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.team.TeamRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeRequest;
import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TaskMapper {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TagMapper tagMapper;

    public TaskResponse toTaskResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .projectKey(t.getProject().getKey())
                .projectId(t.getProject().getId())
                .name(t.getName())
                .description(t.getDescription())
                .state(t.getState().name())
                .priority(t.getPriority().name())
                .type(t.getType().name())
                .storyPoints(t.getStoryPoints())
                .assignees(t.getAssignees() != null ?
                        t.getAssignees().stream()
                                .map(a -> {
                                    if (a.getAssigneeType() == AssigneeType.USER) {
                                        User user = userRepository.findById(a.getAssigneeId())
                                                .orElse(null);
                                        return AssigneeRequest.builder()
                                                .assigneeId(user != null ? user.getId() : null)
                                                .email(user != null ? user.getEmail() : null)
                                                .dynamicAssign(true)
                                                .type("USER")
                                                .build();
                                    } else if (a.getAssigneeType() == AssigneeType.TEAM) {
                                        Team team = teamRepository.findById(a.getAssigneeId())
                                                .orElse(null);
                                        return AssigneeRequest.builder()
                                                .assigneeId(team != null ? team.getId() : null)
                                                .email(Objects.requireNonNull(team).getName()+" 팀")
                                                .dynamicAssign(true)
                                                .type("TEAM")
                                                .build();
                                    } else {
                                        return null;
                                    }
                                }).filter(Objects::nonNull)
                                .toList() : null)
                .sprintId(t.getSprint() == null ? null : t.getSprint().getId())
                .milestoneId(t.getMilestone() == null ? null : t.getMilestone().getId())
                .tags(
                        t.getTags().stream()
                                .map(tagMapper::toResponse)
                                .toList()
                )
                .dependencyIds(t.getDependencies().stream().map(d -> d.getDependsOn().getId()).toList())
                .dueDate(t.getDueDate())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

