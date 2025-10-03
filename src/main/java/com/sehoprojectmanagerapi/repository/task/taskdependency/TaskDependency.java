package com.sehoprojectmanagerapi.repository.task;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_dependencies",
        uniqueConstraints = @UniqueConstraint(name = "uk_task_dep_pair", columnNames = {"task_id", "depends_on_task_id"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDependency {
    @EmbeddedId
    private TaskDependencyId id = new TaskDependencyId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("dependsOnTaskId")
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DependencyType type = DependencyType.BLOCKS;
}
