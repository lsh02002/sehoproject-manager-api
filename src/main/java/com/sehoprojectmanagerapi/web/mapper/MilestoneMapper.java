package com.sehoprojectmanagerapi.web.mapper;

import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MilestoneMapper {
    private final TaskMapper taskMapper;

    public MilestoneResponse toResponse(Milestone milestone) {
        return MilestoneResponse.builder()
                .id(milestone.getId())
                .projectId(milestone.getProject().getId())
                .name(milestone.getName())
                .description(milestone.getDescription())
                .startDate(milestone.getStartDate())
                .dueDate(milestone.getDueDate())
                .status(milestone.getStatus().name())
                .taskIds(milestone.getTasks().stream().map(taskMapper::toTaskResponse).toList())
                .createdAt(milestone.getCreatedAt())
                .updatedAt(milestone.getUpdatedAt())
                .build();
    }
}

