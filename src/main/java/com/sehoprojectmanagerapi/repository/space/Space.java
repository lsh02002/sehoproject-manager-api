package com.sehoprojectmanagerapi.repository.space;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.common.Visibility;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.space.spacemember.SpaceMember;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spaces",
        uniqueConstraints = {
                // 같은 워크스페이스 내에서 이름 중복 방지
                @UniqueConstraint(columnNames = {"workspace_id", "name"}),
                @UniqueConstraint(columnNames = {"workspace_id", "slug"})
        },
        indexes = {
                @Index(columnList = "workspace_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Space extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Visibility visibility = Visibility.INTERNAL;

    @ManyToOne(fetch = FetchType.LAZY)              // ✅ 작성자
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private int position = 0;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceMember> spaceMembers = new ArrayList<>();

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Project> projects = new ArrayList<>();

    // 편의 메서드
    public void addProject(Project project) {
        if (project == null) return;
        if (!projects.contains(project)) projects.add(project);
        project.setSpace(this);
    }

    public void removeProject(Project project) {
        if (project == null) return;
        projects.remove(project);
        if (project.getSpace() == this) project.setSpace(null);
    }
}
