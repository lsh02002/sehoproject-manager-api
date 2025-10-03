package com.sehoprojectmanagerapi.web.controller.project;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.project.ProjectService;
import com.sehoprojectmanagerapi.web.dto.project.ProjectInviteRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectInviteResponse;
import com.sehoprojectmanagerapi.web.dto.project.ProjectRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjectsByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(projectService.getAllTeamsByUser(customUserDetails.getId()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(customUserDetails.getId(), projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.createProject(customUserDetails.getId(), projectRequest));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId, @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.updateProject(customUserDetails.getId(), projectId, projectRequest));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        projectService.deleteProject(customUserDetails.getId(), projectId);
        return ResponseEntity.ok().build();
    }

    /* ==== 멤버 초대 (기존 PathVar 방식 개선: Body로 받기) ==== */
    @PostMapping("/{projectId}/invites")
    public ResponseEntity<ProjectInviteResponse> inviteToProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId,
            @RequestBody ProjectInviteRequest request
    ) {
        ProjectInviteResponse invite = projectService.inviteToProject(user.getId(), projectId, request);
        return ResponseEntity.ok(invite);
    }

    /* ==== 초대 수락 ==== */
    @PostMapping("/{projectId}/invites/{inviteId}/accept")
    public ResponseEntity<ProjectResponse> acceptInvite(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId,
            @PathVariable Long inviteId
    ) {
        return ResponseEntity.ok(projectService.acceptInvite(user.getId(), projectId, inviteId));
    }

    /* ==== 초대 거절 ==== */
    @PostMapping("/{projectId}/invites/{inviteId}/decline")
    public ResponseEntity<Void> declineInvite(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId,
            @PathVariable Long inviteId
    ) {
        projectService.declineInvite(user.getId(), projectId, inviteId);
        return ResponseEntity.noContent().build();
    }
}
