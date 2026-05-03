package com.sehoprojectmanagerapi.web.controller.task;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.task.TaskService;
import com.sehoprojectmanagerapi.web.dto.task.TaskRequest;
import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/assignee/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getTasksByAssigneeId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByAssigneeId(customUserDetails.getId(), projectId));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaskResponse> createTask(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestPart TaskRequest request, @RequestPart(required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(taskService.createTask(customUserDetails.getId(), request, files));
    }

    @PostMapping(value = "/{taskId}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaskResponse> updateTask(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long taskId, @RequestPart TaskUpdateRequest request, @RequestPart(required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(taskService.updateTask(customUserDetails.getId(), taskId, request, files));
    }

    @DeleteMapping("/{taskId}/projects/{projectId}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(customUserDetails.getId(), projectId, taskId);
        return ResponseEntity.ok().build();
    }
}
