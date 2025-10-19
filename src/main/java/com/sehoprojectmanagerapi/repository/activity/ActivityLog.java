package com.sehoprojectmanagerapi.repository.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.user.User;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_project_created", columnList = "project_id,created_at")
})

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLog extends BaseEntity {
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

    @Column
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private ActivityAction action;

    @Type(JsonType.class)
    @Column(name = "before_json", columnDefinition = "JSON")
    private Map<String, Object> beforeJson;

    @Type(JsonType.class)
    @Column(name = "after_json", columnDefinition = "JSON")
    private Map<String, Object> afterJson;

    public ActivityLog(ActivityEntityType type, ActivityAction action, Long targetId, String message, User actor, Project project, Map<String, Object> beforeJson, Map<String, Object> afterJson) {
        this.entityType = type;
        this.action = action;
        this.entityId = targetId;
        this.message = message;
        this.actor = actor;
        this.project = project;
        this.beforeJson = beforeJson;
        this.afterJson =afterJson;
    }
}
