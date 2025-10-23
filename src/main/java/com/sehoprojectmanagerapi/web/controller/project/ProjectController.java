package com.sehoprojectmanagerapi.web.controller.project;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.project.ProjectService;
import com.sehoprojectmanagerapi.web.dto.project.ProjectRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjectsByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(projectService.getAllTeamsByUser(customUserDetails.getId()));
    }

    @GetMapping("/spaces/{spaceId}")
    public ResponseEntity<List<ProjectResponse>> getAllProjectsByUserAndSpace(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long spaceId) {
        return ResponseEntity.ok(projectService.getAllProjectsByUserAndSpace(customUserDetails.getId(), spaceId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(customUserDetails.getId(), projectId));
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectResponse> createProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.createProject(customUserDetails.getId(), projectRequest));
    }

    @PutMapping("/{projectId}/edit")
    public ResponseEntity<ProjectResponse> updateProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId, @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.updateProject(customUserDetails.getId(), projectId, projectRequest));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        projectService.deleteProject(customUserDetails.getId(), projectId);
        return ResponseEntity.ok().build();
    }
}
