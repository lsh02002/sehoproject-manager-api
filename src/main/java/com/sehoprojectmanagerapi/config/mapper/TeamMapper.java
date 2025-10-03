package com.sehoprojectmanagerapi.config.mapper;

// TeamMapper.java

import com.sehoprojectmanagerapi.repository.team.teamInvite.TeamInvite;
import com.sehoprojectmanagerapi.web.dto.team.TeamInviteResponse;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public TeamInviteResponse toInviteResponse(TeamInvite invite) {
        return new TeamInviteResponse(
                invite.getId(),
                invite.getTeam().getId(),
                invite.getInviter().getId(),
                invite.getInvitedUser().getId(),
                invite.getMessage(),
                invite.getRequestedRole(),
                invite.getStatus().name(),
                invite.getExpiresAt(),
                invite.getCreatedAt()
        );
    }

    // convertToTeamResponse(..) 는 기존 구현 사용
}

