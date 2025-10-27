package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.task.taskassignee.AssigneeType;
import com.sehoprojectmanagerapi.repository.task.taskassignee.TaskAssignee;
import com.sehoprojectmanagerapi.repository.task.taskdependency.TaskDependency;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_project_state_priority_due", columnList = "project_id,state,priority,due_date")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Task extends BaseEntity implements Loggable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Task parent;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Column(nullable = false)
    private int position = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskState state = TaskState.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskPriority priority = TaskPriority.MEDIUM;

    private Integer storyPoints;

    @Column(precision = 12, scale = 6)
    private BigDecimal ordinal;

    private LocalDate startDate;
    private LocalDate dueDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "closed_by")
    private User closedBy;

    private OffsetDateTime closedAt;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "milestone_id")
    private Milestone milestone; // null 가능

    @ManyToMany
    @JoinTable(name = "task_tag",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = {
                    @UniqueConstraint(name = "ux_task_tag_task_tag", columnNames = {"task_id", "tag_id"})
            }
    )
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAssignee> assignees = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<TaskDependency> dependencies = new ArrayList<>();

    @Override
    public ActivityEntityType logTargetType() {
        return ActivityEntityType.TASK;
    }

    @Override
    public Long logTargetId() {
        return id;
    }

    @Override
    public String logMessage() {
        return "name=" + name;
    }

    @Override
    public Project logProject() {
        return this.project;
    }

    /* -------------------------
       편의 메서드 (양방향 일관성 보장)
       ------------------------- */

    // Assignee
    // Task 엔티티 내부
    public void addAssignee(User user, User actor) {
        TaskAssignee ta = TaskAssignee.forUser(this, user, actor, OffsetDateTime.now());
        ta.setTask(this);
        assignees.add(ta);
    }

    public void addAssignee(Team team, User actor, boolean dynamic) {
        TaskAssignee ta = TaskAssignee.forTeam(this, team, actor, dynamic, OffsetDateTime.now());
        ta.setTask(this);
        assignees.add(ta);
    }

    public void removeAssignee(User user) {
        assignees.removeIf(ta ->
                ta.getAssigneeType() == AssigneeType.USER &&
                        Objects.equals(ta.getAssigneeId(), user.getId())
        );
    }

    public void removeAssignee(Team team) {
        assignees.removeIf(ta ->
                ta.getAssigneeType() == AssigneeType.TEAM &&
                        Objects.equals(ta.getAssigneeId(), team.getId())
        );
    }

    // Tag
    public void addTag(Tag tag) {
        if (tag == null) return;
        if (!tags.contains(tag)) {
            tags.add(tag);
            // 역방향 동기화 (선택)
            tag.getTasks().add(this);
        }
    }

    public void removeTag(Tag tag) {
        if (tag == null) return;
        tags.remove(tag);
        tag.getTasks().remove(this);
    }

    // Dependency
    public void addDependency(Task dependOn) {
        if (Objects.equals(this.id, dependOn.getId())) {
            throw new IllegalArgumentException("자기 자신을 의존성으로 설정할 수 없습니다.");
        }
        TaskDependency d = new TaskDependency(this, dependOn);
        dependencies.add(d);
    }

    public void removeDependency(Long prerequisiteTaskId) {
        dependencies.removeIf(d -> Objects.equals(d.getDependsOn().getId(), prerequisiteTaskId));
    }
}
