package com.sehoprojectmanagerapi.repository.milestone;

import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.task.Task;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "milestones")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Milestone extends BaseEntity implements Loggable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private int position = 0;

    @Column(columnDefinition = "text")
    private String description;

    private LocalDate startDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MilestoneStatus status = MilestoneStatus.PLANNED;

    @OneToMany(mappedBy = "milestone")
    private List<Task> tasks = new ArrayList<>();

    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            for (Task task : tasks) {
                addTask(task);
            }
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.setMilestone(this); // 양방향 연결
    }

    @Override
    public String logMessage() {
        return "프로젝트 아이디: '" + project.getId() + "' 의 마일스톤 '" + name + "'";
    }
}