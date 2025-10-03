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
public class TaskMilestoneId implements java.io.Serializable {
    private Long taskId;
    private Long milestoneId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskMilestoneId other)) return false;
        return Objects.equals(taskId, other.taskId) &&
                Objects.equals(milestoneId, other.milestoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, milestoneId);
    }
}
