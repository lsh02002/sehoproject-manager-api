package com.sehoprojectmanagerapi.repository.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_team_key", columnNames = {"team_id", "project_key"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_key", length = 32, nullable = true)
    private String key;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    private LocalDate startDate;
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * 이 프로젝트에 속한 모든 팀(1:N).
     * Team 쪽에 @ManyToOne Project project 가 있어야 합니다.
     * JSON 순환 방지를 위해 @JsonIgnore 권장(필요 시 DTO로 분리).
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Team> teams = new ArrayList<>();

    public void addTeam(Team team) {
        if (team == null) return;

        // 다른 프로젝트에 붙어 있으면 먼저 떼기 (선택)
        if (team.getProject() != null && team.getProject() != this) {
            team.getProject().removeTeam(team);
        }

        if (!this.teams.contains(team)) { // 중복 방지
            this.teams.add(team);
        }
        // 역방향 동기화
        if (team.getProject() != this) {
            team.setProject(this);
        }
    }

    public void removeTeam(Team team) {
        if (team == null) return;
        this.teams.remove(team);
        if (team.getProject() == this) {
            team.setProject(null);
        }
    }
}
