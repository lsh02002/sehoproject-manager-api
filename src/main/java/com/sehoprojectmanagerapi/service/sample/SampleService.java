package com.sehoprojectmanagerapi.service.sample;

import com.sehoprojectmanagerapi.service.project.ProjectService;
import com.sehoprojectmanagerapi.service.space.SpaceService;
import com.sehoprojectmanagerapi.service.task.TaskService;
import com.sehoprojectmanagerapi.service.workspace.WorkspaceService;
import com.sehoprojectmanagerapi.web.dto.project.ProjectRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import com.sehoprojectmanagerapi.web.dto.space.SpaceRequest;
import com.sehoprojectmanagerapi.web.dto.space.SpaceResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SampleService {

    private final WorkspaceService workspaceService;
    private final SpaceService spaceService;
    private final ProjectService projectService;
    private final TaskService taskService;

    @Transactional
    public void createSample(Long userId, WorkspaceRequest workspaceRequest) {
        WorkspaceResponse workspaceResponse = workspaceService.createWorkspace(userId, workspaceRequest);

        SpaceRequest spaceRequest = SpaceRequest.builder()
                .name("Team Space")
                .slug("team-space")
                .build();

        SpaceResponse spaceResponse = spaceService.createSpace(userId, workspaceResponse.id(), spaceRequest);

        ProjectRequest projectRequest = ProjectRequest.builder()
                .spaceId(spaceResponse.id())
                .projectKey("BACKEND")
                .name("Project1")
                .description("This is a test project1")
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now())
                .creatorId(userId)
                .build();

        ProjectResponse projectResponse = projectService.createProject(userId, projectRequest);

        TaskRequest taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getProjectId())
                .title("Task1")
                .description("This is a test task1")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getProjectId())
                .title("Task2")
                .description("This is a test task2")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getProjectId())
                .title("Task3")
                .description("This is a test task3")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        ProjectRequest projectRequest2 = ProjectRequest.builder()
                .spaceId(spaceResponse.id())
                .projectKey("FRONTEND")
                .name("Project2")
                .description("This is a test project2")
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now())
                .creatorId(userId)
                .build();

        ProjectResponse projectResponse2 = projectService.createProject(userId, projectRequest2);

        TaskRequest taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getProjectId())
                .title("Task1")
                .description("This is a test task1")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);

        taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getProjectId())
                .title("Task2")
                .description("This is a test task2")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);

        taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getProjectId())
                .title("Task3")
                .description("This is a test task3")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);
    }
}
