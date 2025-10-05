package com.sehoprojectmanagerapi.repository.team.teammember;

import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "team_members")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamMember {
    @EmbeddedId
    private TeamMemberId id = new TeamMemberId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RoleTeam role = RoleTeam.MEMBER;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    private OffsetDateTime joinedAt;
}
