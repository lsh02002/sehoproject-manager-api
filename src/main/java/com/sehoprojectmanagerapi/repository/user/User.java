package com.sehoprojectmanagerapi.repository.user;

import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity implements Loggable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255, unique = true, nullable = false)
    private String nickname;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(length = 64)
    private String timezone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "workspace_id")
    private Long workspaceId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private String userStatus;

    @Override
    public String logMessage() {
        return "사용자 '" + nickname + "'";
    }
}
