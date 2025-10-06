package com.sehoprojectmanagerapi.web.mapper;

// TeamMapper.java

import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.team.teamInvite.TeamInvite;
import com.sehoprojectmanagerapi.web.dto.team.TeamInviteResponse;
import com.sehoprojectmanagerapi.web.dto.team.TeamResponse;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public TeamInviteResponse toInviteResponse(TeamInvite invite) {
        return TeamInviteResponse.builder()
                .id(invite.getId())
                .teamId(invite.getTeam().getId())
                .inviterId(invite.getInviter().getId())
                .invitedUserId(invite.getInvitedUser().getId())
                .message(invite.getMessage())
                .requestedRole(invite.getRequestedRole())
                .status(invite.getStatus().name())
                .expiresAt(invite.getExpiresAt())
                .createdAt(invite.getCreatedAt())
                .build();

    }

    public TeamResponse toTeamResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .build();
    }
}

