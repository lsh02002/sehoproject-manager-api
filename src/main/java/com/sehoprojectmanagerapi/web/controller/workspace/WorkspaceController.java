package com.sehoprojectmanagerapi.web.controller.workspace;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.workspace.WorkspaceService;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/tree")
    public ResponseEntity<List<TreeRow>> getWorkspaceTree() {
        return ResponseEntity.ok(workspaceService.getTreeRowsForCurrentUser());
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                   @RequestBody WorkspaceRequest request) {
        return ResponseEntity.ok(workspaceService.createWorkspace(customUserDetails.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> listWorkspaces(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(workspaceService.listWorkspaces(customUserDetails.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                 @PathVariable Long id) {
        return ResponseEntity.ok(workspaceService.getWorkspace(customUserDetails.getId(), id));
    }

    @PutMapping("/{id}")
    public WorkspaceResponse updateWorkspace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                    @PathVariable Long id,
                                    @RequestBody WorkspaceRequest request) {
        return workspaceService.updateWorkspace(customUserDetails.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                       @PathVariable Long id) {
        workspaceService.deleteWorkspace(customUserDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /* ==== 멤버 초대 (기존 PathVar 방식 개선: Body로 받기) ==== */
    @GetMapping("/invitations")
    public ResponseEntity<List<WorkspaceInviteResponse>> getMyInvites(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(workspaceService.getMyWorkspaceInvites(customUserDetails.getId()));
    }

    @PostMapping("/{workspaceId}/invites")
    public ResponseEntity<WorkspaceInviteResponse> inviteToWorkspace(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long workspaceId,
            @RequestBody WorkspaceInviteRequest request
    ) {
        WorkspaceInviteResponse invite = workspaceService.inviteToWorkspace(customUserDetails.getId(), workspaceId, request);
        return ResponseEntity.ok(invite);
    }

    /* ==== 초대 수락 ==== */
    @PostMapping("/{workspaceId}/invites/{inviteId}/accept")
    public ResponseEntity<WorkspaceResponse> acceptInvite(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long inviteId
    ) {
        return ResponseEntity.ok(workspaceService.acceptWorkspaceInvite(customUserDetails.getId(), workspaceId, inviteId));
    }

    /* ==== 초대 거절 ==== */
    @PostMapping("/{workspaceId}/invites/{inviteId}/decline")
    public ResponseEntity<WorkspaceResponse> declineInvite(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long inviteId
    ) {
        return ResponseEntity.ok(workspaceService.declineWorkspaceInvite(customUserDetails.getId(), workspaceId, inviteId));
    }
}
