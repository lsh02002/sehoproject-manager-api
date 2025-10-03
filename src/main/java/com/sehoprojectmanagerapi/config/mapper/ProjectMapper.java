package com.sehoprojectmanagerapi.config.mapper;

// ProjectMapper.java

import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectinvite.ProjectInvite;
import com.sehoprojectmanagerapi.web.dto.project.ProjectInviteResponse;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(),                              // projectId
                project.getTeam() != null ? project.getTeam().getId() : null, // teamId
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name(),                   // Enum → String
                project.getStartDate(),
                project.getDueDate(),
                project.getCreatedBy() != null ? project.getCreatedBy().getName() : null // creatorName
        );
    }

    public ProjectInviteResponse toInviteResponse(ProjectInvite invite) {
        return new ProjectInviteResponse(
                invite.getId(),
                invite.getProject().getId(),
                invite.getInviter().getId(),
                invite.getInvitedUser().getId(),
                invite.getMessage(),
                invite.getRequestedRole(),
                invite.getStatus().name(),
                invite.getExpiresAt(),
                invite.getCreatedAt()
        );
    }
}
