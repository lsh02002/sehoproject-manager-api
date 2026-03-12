package com.sehoprojectmanagerapi.config.function;

import com.sehoprojectmanagerapi.repository.attachment.Attachment;
import com.sehoprojectmanagerapi.repository.comment.Comment;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.notification.Notification;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.space.spacemember.SpaceMember;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.taskdependency.TaskDependency;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMember;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SnapshotFunc {
    public Map<String, Object> snapshot(Object obj) {
        if (obj == null) return null;

        if(obj instanceof Attachment attachment) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", attachment.getId());
            m.put("taskId", attachment.getTask().getId());
            m.put("projectId", attachment.getTask().getProject().getId());
            m.put("uploaderId", attachment.getUploader() != null ? attachment.getUploader().getId() : null);
            m.put("fileName", attachment.getFileName());
            m.put("fileUrl", attachment.getFileUrl());
            m.put("mimeType", attachment.getMimeType());
            m.put("sizeBytes", attachment.getSizeBytes());
            m.put("deleted", attachment.getDeleted());
            m.put("logMessage", attachment.logMessage());
            return m;
        }

        // Comment 객체일 경우
        if (obj instanceof Comment comment) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", comment.getId());
            m.put("taskId", comment.getTask().getId());
            m.put("projectId", comment.getTask().getProject().getId());
            m.put("authorId", comment.getAuthor() != null ? comment.getAuthor().getId() : null);
            m.put("createdAt", comment.getCreatedAt());
            m.put("body", comment.getBody() != null ? comment.getBody() : null);
            m.put("logMessage", comment.logMessage());
            return m;
        }

        if (obj instanceof Project project) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", project.getId());
            m.put("key", project.getKey());
            m.put("name", project.getName());
            m.put("position", project.getPosition());
            m.put("description", project.getDescription());
            m.put("startDate", project.getStartDate());
            m.put("dueDate", project.getDueDate());
            m.put("milestones", project.getMilestones() != null ? project.getMilestones().stream().map(Milestone::getId).toList() : null);
            m.put("sprints", project.getSprints() != null ? project.getSprints().stream().map(Sprint::getId).toList() : null);
            m.put("tasks", project.getTasks() != null ? project.getTasks().stream().map(Task::getId).toList() : null);
            m.put("tags", project.getTags() != null ? project.getTags().stream().map(Tag::getId).toList() : null);
            m.put("createdBy", project.getCreatedBy().getId());
            m.put("spaceId", project.getSpace().getId());
            m.put("createdAt", project.getCreatedAt());
            m.put("logMessage", project.logMessage());
            return m;
        }

        if(obj instanceof ProjectMember projectMember) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", projectMember.getId());
            m.put("projectId", projectMember.getProject().getId());
            m.put("role", projectMember.getRole());
            m.put("userId", projectMember.getUser().getId());
            m.put("createdAt", projectMember.getCreatedAt());
            m.put("logMessage", projectMember.logMessage());
            return m;
        }

        // Comment 객체일 경우
        if (obj instanceof Milestone milestone) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", milestone.getId());
            m.put("projectId", milestone.getProject().getId());
            m.put("name", milestone.getName());
            m.put("position", milestone.getPosition());
            m.put("description", milestone.getDescription());
            m.put("status", milestone.getStatus());
            m.put("startDate", milestone.getStartDate());
            m.put("dueDate", milestone.getDueDate());
            m.put("tasks", milestone.getTasks() != null ? milestone.getTasks().stream().map(Task::getId).toList() : null);
            m.put("createdAt", milestone.getCreatedAt());
            m.put("logMessage", milestone.logMessage());
            return m;
        }

        if (obj instanceof Sprint sprint) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", sprint.getId());
            m.put("projectId", sprint.getProject().getId());
            m.put("name", sprint.getName());
            m.put("position", sprint.getPosition());
            m.put("startDate", sprint.getStartDate());
            m.put("endDate", sprint.getEndDate());
            m.put("state", sprint.getState());
            m.put("tasks", sprint.getTasks() != null ? sprint.getTasks().stream().map(Task::getId).toList() : null);
            m.put("createdAt", sprint.getCreatedAt());
            m.put("logMessage", sprint.logMessage());
            return m;
        }

        if (obj instanceof Tag tag) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", tag.getId());
            m.put("projectId", tag.getProject().getId());
            m.put("name", tag.getName());
            m.put("description", tag.getDescription());
            m.put("createdAt", tag.getCreatedAt());
            m.put("logMessage", tag.logMessage());
            return m;
        }

        if (obj instanceof Task task) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", task.getId());
            m.put("projectId", task.getProject().getId());
            m.put("name", task.getName());
            m.put("parentId", task.getParent() != null ? task.getParent().getId() : null);
            m.put("description", task.getDescription());
            m.put("type", task.getType());
            m.put("position", task.getPosition());
            m.put("state", task.getState());
            m.put("priority", task.getPriority());
            m.put("storyPoints", task.getStoryPoints());
            m.put("ordinal", task.getOrdinal());
            m.put("startDate", task.getStartDate());
            m.put("dueDate", task.getDueDate());
            m.put("createdById", task.getCreatedBy().getId());
            m.put("closedById", task.getClosedBy() != null ? task.getClosedBy().getId() : null);
            m.put("closedAt", task.getClosedAt());
            m.put("createdAt", task.getCreatedAt());
            m.put("sprintId", task.getSprint() != null ? task.getSprint().getId() : null);
            m.put("milestoneId", task.getMilestone() != null ? task.getMilestone().getId() : null);
            m.put("tags", task.getTags() != null ? task.getTags().stream().map(Tag::getId).toList() : null);
            m.put("assignees",
                    task.getAssignees() != null
                            ? task.getAssignees().stream()
                            .map(a -> Map.of(
                                    "assigneeId", a.getAssigneeId(),
                                    "assigneeType", a.getAssigneeType()
                            ))
                            .toList()
                            : null
            );
            m.put("dependencies", task.getDependencies() != null ? task.getDependencies().stream().map(TaskDependency::getId).toList() : null);
            m.put("logMessage", task.logMessage());
            return m;
        }

        if(obj instanceof Notification notification) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", notification.getId());
            m.put("receiverId", notification.getReceiver());
            m.put("message", notification.getMessage());
            m.put("type", notification.getType());
            m.put("relatedId", notification.getRelatedId());
            m.put("readFlag", notification.isReadFlag());
            m.put("logMessage", notification.logMessage());
            m.put("createdAt", notification.getCreatedAt());
            return m;
        }

        if(obj instanceof Space space) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", space.getId());
            m.put("workspaceId", space.getWorkspace().getId());
            m.put("name", space.getName());
            m.put("slug", space.getSlug());
            m.put("visibility", space.getVisibility());
            m.put("createdBy", space.getCreatedBy());
            m.put("position", space.getPosition());
            m.put("logMessage", space.logMessage());
            m.put("createdAt", space.getCreatedAt());
            return m;
        }

        if(obj instanceof SpaceMember spaceMember) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", spaceMember.getId());
            m.put("spaceId", spaceMember.getSpace().getId());
            m.put("userId", spaceMember.getUser().getId());
            m.put("status", spaceMember.getStatus());
            m.put("role", spaceMember.getRole());
            m.put("joinedAt", spaceMember.getJoinedAt());
            m.put("logMessage", spaceMember.logMessage());
            m.put("createdAt", spaceMember.getCreatedAt());
            return m;
        }

        if(obj instanceof Workspace workspace) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", workspace.getId());
            m.put("name", workspace.getName());
            m.put("slug", workspace.getSlug());
            m.put("visibility", workspace.getVisibility());
            m.put("createdBy", workspace.getCreatedBy().getId());
            m.put("position", workspace.getPosition());
            m.put("logMessage", workspace.logMessage());
            m.put("createdAt", workspace.getCreatedAt());
            return m;
        }

        if(obj instanceof WorkspaceMember workspaceMember) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", workspaceMember.getId());
            m.put("workspaceId", workspaceMember.getWorkspace().getId());
            m.put("userId", workspaceMember.getUser().getId());
            m.put("role", workspaceMember.getRole());
            m.put("status", workspaceMember.getStatus());
            m.put("joinedAt", workspaceMember.getJoinedAt());
            m.put("logMessage", workspaceMember.logMessage());
            m.put("createdAt", workspaceMember.getCreatedAt());
            return m;
        }

        if (obj instanceof User user) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", user.getId());
            m.put("email", user.getEmail());
            m.put("password", user.getPasswordHash());
            m.put("nickname", user.getNickname());
            m.put("userStatus", user.getUserStatus());
            m.put("deletedAt", user.getDeletedAt());
            m.put("createdAt", user.getCreatedAt());
            return m;
        }
        // 다른 엔티티 타입이 들어오면 필요한 경우 여기에 추가
        // if (obj instanceof Sprint s) { ... }
        // if (obj instanceof Issue i) { ... }

        // 매칭되는 타입이 없으면 null 반환 또는 단순 toString()
        return Map.of("value", String.valueOf(obj));
    }
}
