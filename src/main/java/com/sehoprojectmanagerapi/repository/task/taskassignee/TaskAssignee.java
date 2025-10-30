package com.sehoprojectmanagerapi.repository.task.taskassignee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task_assignees")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class TaskAssignee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    private AssigneeType assigneeType; // USER or TEAM

    @Column(nullable = false)
    private Long assigneeId; // user.id or team.id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_dynamic", nullable = false)
    private boolean dynamic; // 팀 할당 시 멤버 변동 자동 동기화 여부

    @OneToMany(mappedBy = "sourceAssignee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TaskAssigneeUser> expandedUsers = new ArrayList<>();

    private OffsetDateTime assignedAt;

    // id는 제외한 생성자
    private TaskAssignee(Task task,
                         AssigneeType type,
                         Long assigneeId,
                         User createdBy,
                         boolean dynamic,
                         OffsetDateTime assignedAt) {
        if (task == null || type == null || assigneeId == null || createdBy == null || assignedAt == null) {
            throw new IllegalArgumentException("TaskAssignee requires non-null arguments");
        }
        this.task = task;
        this.assigneeType = type;
        this.assigneeId = assigneeId;
        this.createdBy = createdBy;
        this.dynamic = dynamic;
        this.assignedAt = assignedAt;
    }

    // --- 정적 팩토리: 사용자 담당자 ---
    public static TaskAssignee forUser(Task task, User user, User actor, OffsetDateTime at) {
        return new TaskAssignee(task, AssigneeType.USER, user.getId(), actor, false, at);
    }

    // --- 정적 팩토리: 팀 담당자 ---
    public static TaskAssignee forTeam(Task task, Team team, User actor, boolean dynamic, OffsetDateTime at) {
        return new TaskAssignee(task, AssigneeType.TEAM, team.getId(), actor, dynamic, at);
    }

    // 편의 메서드 (검증/조회용)
    public boolean isUserAssignee() {
        return assigneeType == AssigneeType.USER;
    }

    public boolean isTeamAssignee() {
        return assigneeType == AssigneeType.TEAM;
    }
}
