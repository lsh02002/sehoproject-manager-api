package com.sehoprojectmanagerapi.repository.comment;

import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.activity.logger.Loggable;
import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_task_created", columnList = "task_id,created_at")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity implements Loggable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "text", nullable = false)
    private String body;

    @Override
    public ActivityEntityType logTargetType() {
        return ActivityEntityType.COMMENT;
    }

    @Override
    public Long logTargetId() {
        return id;
    }

    @Override
    public String logMessage() {
        return "name=" + body;
    }

    @Override
    public Project logProject() {
        return this.task.getProject();
    }
}
