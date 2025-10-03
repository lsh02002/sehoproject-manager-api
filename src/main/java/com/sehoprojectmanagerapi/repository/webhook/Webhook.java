package com.sehoprojectmanagerapi.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "webhooks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "target_url", length = 2048, nullable = false)
    private String targetUrl;

    @Column(length = 255)
    private String secret;

    @Column(name = "event_mask", columnDefinition = "jsonb")
    private String eventMask;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    private OffsetDateTime createdAt;
}
