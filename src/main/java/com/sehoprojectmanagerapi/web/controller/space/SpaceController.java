package com.sehoprojectmanagerapi.web.controller.space;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.space.SpaceService;
import com.sehoprojectmanagerapi.web.dto.space.SpaceRequest;
import com.sehoprojectmanagerapi.web.dto.space.SpaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspaces/{workspaceId}/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    public ResponseEntity<SpaceResponse> createSpace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @PathVariable Long workspaceId,
                                               @RequestBody SpaceRequest request) {
        return ResponseEntity.ok(spaceService.createSpace(customUserDetails.getId(), workspaceId, request));
    }

    @GetMapping
    public ResponseEntity<List<SpaceResponse>> listSpaces(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                    @PathVariable Long workspaceId) {
        return ResponseEntity.ok(spaceService.listSpaces(customUserDetails.getId(), workspaceId));
    }

    @GetMapping("/{spaceId}")
    public ResponseEntity<SpaceResponse> getSpace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                             @PathVariable Long workspaceId,
                             @PathVariable Long spaceId) {
        return ResponseEntity.ok(spaceService.getSpace(customUserDetails.getId(), workspaceId, spaceId));
    }

    @PutMapping("/{spaceId}")
    public ResponseEntity<SpaceResponse> updateSpace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                @PathVariable Long workspaceId,
                                @PathVariable Long spaceId,
                                @RequestBody SpaceRequest request) {
        return ResponseEntity.ok(spaceService.updateSpace(customUserDetails.getId(), workspaceId, spaceId, request));
    }

    @DeleteMapping("/{spaceId}")
    public ResponseEntity<Void> deleteSpace(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                       @PathVariable Long workspaceId,
                       @PathVariable Long spaceId) {
        spaceService.deleteSpace(customUserDetails.getId(), workspaceId, spaceId);
        return ResponseEntity.noContent().build();
    }
}
