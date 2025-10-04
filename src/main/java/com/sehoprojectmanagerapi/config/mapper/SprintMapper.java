package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintResponse;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {
    public SprintResponse toResponse(Sprint sprint) {
        return new SprintResponse(
                sprint.getId(),
                sprint.getProject().getId(),
                sprint.getName(),
                sprint.getState().name(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getCreatedAt(),
                sprint.getUpdatedAt()
        );
    }
}
