package com.sehoprojectmanagerapi.repository.task.taskassignee;

import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_assignee_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "user_id"}))
@NoArgsConstructor
public class TaskAssigneeUser extends BaseEntity implements Loggable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_assignee_id", nullable = false)
    private TaskAssignee sourceAssignee;

    @Override
    public String logMessage() {
        return "name=";
    }

    public TaskAssigneeUser(Task task, User assignee, TaskAssignee assigneeSource) {
        this.task = task;
        this.user = assignee;
        this.sourceAssignee = assigneeSource;
    }
}
