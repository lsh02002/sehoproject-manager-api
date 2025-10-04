package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintResponse;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {
    public SprintResponse toResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProject().getId())
                .name(sprint.getName())
                .state(sprint.getState().name())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }
}
