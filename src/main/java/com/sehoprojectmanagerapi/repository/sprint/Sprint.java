package com.sehoprojectmanagerapi.repository.sprint;

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
@Table(name = "sprints")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sprint extends BaseEntity implements Loggable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(length = 255)
    private String name;

    @Column(nullable = false)
    private int position = 0;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SprintState state = SprintState.PLANNED;

    @OneToMany(mappedBy = "sprint")
    private List<Task> tasks = new ArrayList<>();

    @Override
    public ActivityEntityType logTargetType() {
        return ActivityEntityType.SPRINT;
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
}
