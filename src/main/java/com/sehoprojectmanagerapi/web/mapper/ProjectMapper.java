package com.sehoprojectmanagerapi.web.mapper;

// ProjectMapper.java

import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    private final TagMapper tagMapper;

    public ProjectMapper(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())   // projectId
                .projectKey(project.getKey())
                .spaceId(project.getSpace().getId())
                .spaceName(project.getSpace().getName())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name())   // Enum → String
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .creatorId(project.getCreatedBy() != null ? project.getCreatedBy().getId() : null)
                .creatorName(project.getCreatedBy() != null ? project.getCreatedBy().getName() : null)
                .tagResponses(project.getTags() != null ? project.getTags().stream().map(tagMapper::toResponse).toList() : null)
                .build();
    }
}
