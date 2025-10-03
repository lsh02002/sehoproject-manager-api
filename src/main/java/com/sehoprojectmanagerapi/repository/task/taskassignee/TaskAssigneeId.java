package com.sehoprojectmanagerapi.repository.task.taskassignee;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssigneeId implements java.io.Serializable {
    private Long taskId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskAssigneeId other)) return false;
        return Objects.equals(taskId, other.taskId) &&
                Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId);
    }
}
