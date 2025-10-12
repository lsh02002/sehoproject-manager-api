package com.sehoprojectmanagerapi.repository.workspace.workspaceinvite;

import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInvite extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = false)
    private User invitedUser;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String message; // 선택
    @Enumerated(EnumType.STRING)
    private WorkspaceRole requestedRole; // 선택

    private OffsetDateTime expiresAt;

    public enum Status {PENDING, ACCEPTED, DECLINED, EXPIRED}
}
