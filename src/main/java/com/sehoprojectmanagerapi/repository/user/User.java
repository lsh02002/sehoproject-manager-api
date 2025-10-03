package com.sehoprojectmanagerapi.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column(length = 64)
    private String timezone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
