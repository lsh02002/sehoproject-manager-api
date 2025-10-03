package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_milestones")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskMilestone {
    @EmbeddedId
    private TaskMilestoneId id = new TaskMilestoneId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("milestoneId")
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;
}
