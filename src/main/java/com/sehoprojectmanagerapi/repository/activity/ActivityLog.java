package com.sehoprojectmanagerapi.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_project_created", columnList = "project_id,created_at")
})

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 16, nullable = false)
    private ActivityEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private ActivityAction action;

    @Column(name = "before_json", columnDefinition = "jsonb")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "jsonb")
    private String afterJson;

    private OffsetDateTime createdAt;
}
