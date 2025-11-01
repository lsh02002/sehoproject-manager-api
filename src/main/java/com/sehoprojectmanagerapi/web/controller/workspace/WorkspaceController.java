package com.sehoprojectmanagerapi.web.controller.workspace;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.workspace.WorkspaceService;
import com.sehoprojectmanagerapi.web.dto.user.UserInfoResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.TreeRow;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.invite.WorkspaceInviteRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.invite.WorkspaceInviteResponse;
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
    public ResponseEntity<List<TreeRow>> getWorkspaceTree(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(workspaceService.getTreeRowsForCurrentUser(customUserDetails.getId()));
    }

    @PostMapping("/create")
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

    @PostMapping("/invites")
    public ResponseEntity<WorkspaceInviteResponse> inviteToWorkspace(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody WorkspaceInviteRequest request
    ) {
        WorkspaceInviteResponse invite = workspaceService.inviteToWorkspace(customUserDetails.getId(), request);
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

    @GetMapping("/giveprivileges")
    public ResponseEntity<List<WorkspaceInviteResponse>> getGivePrivileges(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(workspaceService.getGivePrivileges(customUserDetails.getId()));
    }
}
