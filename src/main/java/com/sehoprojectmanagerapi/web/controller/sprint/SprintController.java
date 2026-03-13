package com.sehoprojectmanagerapi.web.controller.sprint;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.sprint.SprintService;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintRequest;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sprints")
@RequiredArgsConstructor
@Validated
public class SprintController {
    private final SprintService sprintService;

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<SprintResponse>> getAllVisibleByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(sprintService.getAllSprintsByUserIdAndProjectId(customUserDetails.getId(), projectId));
    }

    @GetMapping("/assignee/project/{projectId}")
    public ResponseEntity<List<SprintResponse>> getSprintsByAssigneeId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(sprintService.getSprintsByAssigneeId(customUserDetails.getId(), projectId));
    }

    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintResponse> getSprintById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.getSprintById(customUserDetails.getId(), sprintId));
    }

    @PostMapping("/create")
    public ResponseEntity<SprintResponse> createSprint(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody SprintRequest request) {
        return ResponseEntity.ok(sprintService.createSprint(customUserDetails.getId(), request));
    }

    @PostMapping("/{sprintId}")
    public ResponseEntity<SprintResponse> updateSprint(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long sprintId, @RequestBody SprintRequest request) {
        return ResponseEntity.ok(sprintService.updateSprint(customUserDetails.getId(), sprintId, request));
    }

    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> deleteSprint(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long sprintId) {
        sprintService.deleteSprint(customUserDetails.getId(), sprintId);
        return ResponseEntity.noContent().build();
    }
}
