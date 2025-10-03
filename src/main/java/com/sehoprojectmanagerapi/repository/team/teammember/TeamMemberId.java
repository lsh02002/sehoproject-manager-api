package com.sehoprojectmanagerapi.repository.team;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberId implements java.io.Serializable {
    private Long teamId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamMemberId other)) return false;
        return Objects.equals(teamId, other.teamId) &&
                Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, userId);
    }
}
