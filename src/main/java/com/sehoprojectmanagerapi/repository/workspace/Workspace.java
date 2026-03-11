package com.sehoprojectmanagerapi.repository.workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.common.Visibility;
import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMember;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspaces",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"slug"}),
                @UniqueConstraint(columnNames = {"name"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace extends BaseEntity implements Loggable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @JsonIgnore
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceMember> workspaceMembers = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Space> spaces = new ArrayList<>();

    @Override
    public String logMessage() {
        return "name=";
    }

    // 편의 메서드
    public void addSpace(Space space) {
        if (space == null) return;
        if (!spaces.contains(space)) spaces.add(space);
        space.setWorkspace(this);
    }

    public void removeSpace(Space space) {
        if (space == null) return;
        spaces.remove(space);
        if (space.getWorkspace() == this) space.setWorkspace(null);
    }
}
