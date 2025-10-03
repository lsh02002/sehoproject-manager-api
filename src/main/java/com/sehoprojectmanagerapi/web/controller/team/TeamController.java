package com.sehoprojectmanagerapi.web.controller.team;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.team.TeamService;
import com.sehoprojectmanagerapi.web.dto.team.TeamInviteRequest;
import com.sehoprojectmanagerapi.web.dto.team.TeamInviteResponse;
import com.sehoprojectmanagerapi.web.dto.team.TeamRequest;
import com.sehoprojectmanagerapi.web.dto.team.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeamsByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(teamService.getAllTeamsByUser(customUserDetails.getId()));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamByUserIdAndTeamId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamByUserIdAndTeamId(customUserDetails.getId(), teamId));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody TeamRequest teamRequest) {
        return ResponseEntity.ok(teamService.createTeam(customUserDetails.getId(), teamRequest));
    }

    @PutMapping("/teamId")
    public ResponseEntity<TeamResponse> updateTeam(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long teamId,  @RequestBody TeamRequest teamRequest) {
        return ResponseEntity.ok(teamService.updateTeam(customUserDetails.getId(), teamId, teamRequest));
    }

    @DeleteMapping("/teamId")
    public ResponseEntity<?> deleteByUserIdAndTeamId(@AuthenticationPrincipal CustomUserDetails customUserDetails, Long teamId) {
        teamService.deleteTeam(customUserDetails.getId(), teamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{teamId}/invites")
    public ResponseEntity<TeamInviteResponse> inviteToTeam(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long teamId,
            @RequestBody TeamInviteRequest request
    ) {
        return ResponseEntity.ok(teamService.inviteToTeam(customUserDetails.getId(), teamId, request));
    }

    @PostMapping("/{teamId}/invites/{inviteId}/accept")
    public ResponseEntity<TeamResponse> acceptTeamInvite(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long teamId,
            @PathVariable Long inviteId
    ) {
        return ResponseEntity.ok(teamService.acceptTeamInvite(customUserDetails.getId(), teamId, inviteId));
    }

    @PostMapping("/{teamId}/invites/{inviteId}/decline")
    public ResponseEntity<Void> declineTeamInvite(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long teamId,
            @PathVariable Long inviteId
    ) {
        teamService.declineTeamInvite(customUserDetails.getId(), teamId, inviteId);
        return ResponseEntity.noContent().build();
    }
}
