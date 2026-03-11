package com.sehoprojectmanagerapi.web.mapper.sprint;

import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintResponse;
import com.sehoprojectmanagerapi.web.mapper.task.TaskMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SprintMapper {
    private final TaskMapper taskMapper;

    public SprintResponse toResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProject().getId())
                .name(sprint.getName())
                .state(sprint.getState().name())
                .taskIds(sprint.getTasks().stream().map(taskMapper::toTaskResponse).toList())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }
}
