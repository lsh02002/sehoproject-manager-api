package com.sehoprojectmanagerapi.repository.task;

import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.task.taskassignee.TaskAssignee;
import com.sehoprojectmanagerapi.repository.task.taskdependency.TaskDependency;
import com.sehoprojectmanagerapi.repository.task.tasktag.TaskTag;
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
public class Task extends BaseEntity {
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
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskType type;

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

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAssignee> assignees = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskDependency> dependencies = new ArrayList<>();

    /* -------------------------
       편의 메서드 (양방향 일관성 보장)
       ------------------------- */

    // Assignee
    public void addAssignee(User user) {
        TaskAssignee ta = new TaskAssignee(this, user, OffsetDateTime.now());
        assignees.add(ta);
    }

    public void removeAssignee(User user) {
        assignees.removeIf(ta -> Objects.equals(ta.getUser().getId(), user.getId()));
    }

    // Tag
    public void addTag(Tag tag) {
        TaskTag tt = new TaskTag(this, tag);
        tags.add(tt);
    }

    public void removeTag(Long tagId) {
        tags.removeIf(t -> t.getTag() != null && Objects.equals(t.getTag().getId(), tagId));
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
