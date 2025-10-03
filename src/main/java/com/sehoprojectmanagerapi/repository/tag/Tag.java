package com.sehoprojectmanagerapi.repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tag_team_name", columnNames = {"team_id", "name"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "color_hex", length = 16)
    private String colorHex;
}
