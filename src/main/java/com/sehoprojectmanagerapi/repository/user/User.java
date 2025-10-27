package com.sehoprojectmanagerapi.repository.user;

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
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255, nullable = false)
    private String name;

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
}
