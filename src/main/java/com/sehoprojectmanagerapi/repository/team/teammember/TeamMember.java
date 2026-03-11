package com.sehoprojectmanagerapi.repository.team.teammember;

import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "team_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamMember extends BaseEntity implements Loggable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 단일 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;  // 팀

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 사용자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RoleTeam role = RoleTeam.MEMBER;  // 팀 내 역할

    @Column(nullable = false)
    private boolean active = true;  // 활동 여부

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;  // 가입 시각

    @Override
    public String logMessage() {
        return "name=";
    }
}
