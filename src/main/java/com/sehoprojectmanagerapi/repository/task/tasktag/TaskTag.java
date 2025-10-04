package com.sehoprojectmanagerapi.repository.task.tasktag;

import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.task.Task;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_tags")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskTag {
    @EmbeddedId
    private TaskTagId id = new TaskTagId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public TaskTag(Task task, Tag tag) {
        this.task = task;
        this.tag = tag;
    }
}
