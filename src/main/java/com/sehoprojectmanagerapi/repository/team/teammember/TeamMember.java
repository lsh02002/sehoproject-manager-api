package com.sehoprojectmanagerapi.repository.team;

import com.sehoprojectmanagerapi.repository.user.RoleTeam;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "team_members")
@Getter
@Setter
@Builder
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

    private OffsetDateTime joinedAt;
}
