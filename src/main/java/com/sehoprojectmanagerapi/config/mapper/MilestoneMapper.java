package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneResponse;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    /** Entity → Response */
    public MilestoneResponse toResponse(Milestone milestone) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getProject().getId(),
                milestone.getTitle(),
                milestone.getDescription(),
                milestone.getStartDate(),
                milestone.getDueDate(),
                milestone.getStatus().name(),
                milestone.getCreatedAt(),
                milestone.getUpdatedAt()
        );
    }
}

