package com.sehoprojectmanagerapi.web.controller.task;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.task.TaskService;
import com.sehoprojectmanagerapi.web.dto.task.TaskRequest;
import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasksByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(taskService.getAllTasksByUser(customUserDetails.getId()));
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<TaskResponse>> getAllTasksByUserAndProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getAllTasksByUserAndProject(customUserDetails.getId(), projectId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(customUserDetails.getId(), taskId));
    }

    @GetMapping("/assignee/workspaces/{workspaceId}")
    public ResponseEntity<List<TaskResponse>> getTasksByAssigneeId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long workspaceId) {
        return ResponseEntity.ok(taskService.getTasksByAssigneeId(customUserDetails.getId(), workspaceId));
    }

    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(customUserDetails.getId(), request));
    }

    @PutMapping("/{taskId}/edit")
    public ResponseEntity<TaskResponse> updateTask(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long taskId, @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(customUserDetails.getId(), taskId, request));
    }

    @DeleteMapping("/{taskId}/projects/{projectId}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(customUserDetails.getId(), projectId, taskId);
        return ResponseEntity.ok().build();
    }
}
