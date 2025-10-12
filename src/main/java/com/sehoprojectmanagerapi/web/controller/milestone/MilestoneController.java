package com.sehoprojectmanagerapi.web.controller.milestone;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.milestone.MilestoneService;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneRequest;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/milestones")
public class MilestoneController {
    private final MilestoneService milestoneService;

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<MilestoneResponse>> getAllMilestonesByUserIdProjectId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(milestoneService.getAllMilestonesByUserIdAndProjectId(customUserDetails.getId(), projectId));
    }

    @GetMapping("/{milestoneId}")
    public ResponseEntity<MilestoneResponse> getMilestoneById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long milestoneId) {
        return ResponseEntity.ok(milestoneService.getMilestoneById(customUserDetails.getId(), milestoneId));
    }

    @PostMapping
    public ResponseEntity<?> createMilestone(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody MilestoneRequest request) {
        return ResponseEntity.ok(milestoneService.createMilestone(customUserDetails.getId(), request));
    }

    @PutMapping("/{milestoneId}")
    public ResponseEntity<?> updateMilestone(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long milestoneId, @RequestBody MilestoneRequest request) {
        return ResponseEntity.ok(milestoneService.updateMilestone(customUserDetails.getId(), milestoneId, request));
    }

    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<?> deleteMilestone(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long milestoneId) {
        milestoneService.deleteMilestone(customUserDetails.getId(), milestoneId);
        return ResponseEntity.ok().build();
    }
}
