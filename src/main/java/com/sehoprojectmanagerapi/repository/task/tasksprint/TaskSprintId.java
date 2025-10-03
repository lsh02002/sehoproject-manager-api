package com.sehoprojectmanagerapi.repository.task.tasksprint;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskSprintId implements java.io.Serializable {
    private Long taskId;
    private Long sprintId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskSprintId other)) return false;
        return Objects.equals(taskId, other.taskId) &&
                Objects.equals(sprintId, other.sprintId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, sprintId);
    }
}
