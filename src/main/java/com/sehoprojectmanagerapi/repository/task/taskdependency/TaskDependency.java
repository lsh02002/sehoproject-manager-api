package com.sehoprojectmanagerapi.repository.task.taskdependency;

import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.task.Task;
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
public class TaskDependency extends BaseEntity implements Loggable {
    @EmbeddedId
    private TaskDependencyId id = new TaskDependencyId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("dependsOnTaskId")
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskDependencyId.DependencyType type = TaskDependencyId.DependencyType.BLOCKS;

    @Override
    public String logMessage() {
        return "name=";
    }

    public TaskDependency(Task task, Task dependsOn) {
        this.task = task;
        this.dependsOn = dependsOn;
    }
}
