package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_sprints")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskSprint {
    @EmbeddedId
    private TaskSprintId id = new TaskSprintId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sprintId")
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;
}
