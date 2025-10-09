package com.sehoprojectmanagerapi.web.controller.workspace;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.workspace.WorkspaceService;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceTreeResponse;
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
    public ResponseEntity<List<WorkspaceTreeResponse>> getWorkspaceTree(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(workspaceService.getWorkspaceTrees(customUserDetails.getId()));
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
}
