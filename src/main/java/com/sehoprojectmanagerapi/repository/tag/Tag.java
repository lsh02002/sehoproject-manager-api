package com.sehoprojectmanagerapi.repository.tag;

import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.task.Task;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_tags_project_name", columnNames = {"project_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;   // 태그는 특정 프로젝트 범위 안에서만 유효하도록

    @ManyToMany(mappedBy = "tags")
    private List<Task> tasks = new ArrayList<>();
}
