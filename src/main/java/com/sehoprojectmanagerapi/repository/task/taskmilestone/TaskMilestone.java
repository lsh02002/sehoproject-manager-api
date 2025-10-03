package com.sehoprojectmanagerapi.repository.task.taskmilestone;

import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.task.Task;
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


    public TaskMilestone(Task task, Milestone milestone) {
        this.task = task;
        this.milestone = milestone;
    }
}
