package com.sehoprojectmanagerapi.repository.task;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDependencyId implements java.io.Serializable {
    private Long taskId;
    private Long dependsOnTaskId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskDependencyId other)) return false;
        return Objects.equals(taskId, other.taskId) &&
                Objects.equals(dependsOnTaskId, other.dependsOnTaskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, dependsOnTaskId);
    }
}
