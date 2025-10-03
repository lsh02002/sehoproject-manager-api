package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "task_assignees")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignee {
    @EmbeddedId
    private TaskAssigneeId id = new TaskAssigneeId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private OffsetDateTime assignedAt;
}
