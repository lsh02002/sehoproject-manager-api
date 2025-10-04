package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneResponse;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    /**
     * Entity → Response
     */
    public MilestoneResponse toResponse(Milestone milestone) {
        return MilestoneResponse.builder()
                .id(milestone.getId())
                .projectId(milestone.getProject().getId())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .startDate(milestone.getStartDate())
                .dueDate(milestone.getDueDate())
                .status(milestone.getStatus().name())
                .createdAt(milestone.getCreatedAt())
                .updatedAt(milestone.getUpdatedAt())
                .build();
    }
}

