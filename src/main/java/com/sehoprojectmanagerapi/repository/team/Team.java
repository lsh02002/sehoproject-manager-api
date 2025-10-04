package com.sehoprojectmanagerapi.repository.team;

import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.team.teammember.TeamMember;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(name = "uk_team_name", columnNames = "name")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Team extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    /**
     * 프로젝트 FK — team.project_id와 항상 일치해야 함.
     * TeamMember 생성 시 무결성 체크를 위해 필요.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> members = new ArrayList<>();
}
