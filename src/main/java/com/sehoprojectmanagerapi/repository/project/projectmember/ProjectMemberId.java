package com.sehoprojectmanagerapi.repository.project.projectmember;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberId implements java.io.Serializable {
    private Long projectId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectMemberId other)) return false;
        return Objects.equals(projectId, other.projectId) &&
                Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, userId);
    }
}
