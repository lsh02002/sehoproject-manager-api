package com.sehoprojectmanagerapi.config.mapper;

// ProjectMapper.java

import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectinvite.ProjectInvite;
import com.sehoprojectmanagerapi.web.dto.project.ProjectInviteResponse;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import com.sehoprojectmanagerapi.web.dto.team.TeamResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .projectId(project.getId())   // projectId
                .teams(
                        project.getTeams().stream()
                                .map(t -> TeamResponse.builder()
                                        .id(t.getId())
                                        .name(t.getName())
                                        .build()
                                ).toList()
                )
                .projectKey(project.getKey())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name())   // Enum → String
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .creatorName(project.getCreatedBy() != null ? project.getCreatedBy().getName() : null)
                .build();

    }

    public ProjectInviteResponse toInviteResponse(ProjectInvite invite) {
        return ProjectInviteResponse.builder()
                .id(invite.getId())
                .projectId(invite.getProject().getId())
                .inviterId(invite.getInviter().getId())
                .invitedUserId(invite.getInvitedUser().getId())
                .message(invite.getMessage())
                .requestedRole(invite.getRequestedRole())
                .status(invite.getStatus().name())
                .expiresAt(invite.getExpiresAt())
                .createdAt(invite.getCreatedAt())
                .build();

    }
}
