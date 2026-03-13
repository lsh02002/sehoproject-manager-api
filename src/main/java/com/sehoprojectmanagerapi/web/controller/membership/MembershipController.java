package com.sehoprojectmanagerapi.web.controller.membership;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.membership.MembershipService;
import com.sehoprojectmanagerapi.web.dto.user.UserInfoResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.AddSpaceAndProjectMembersRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class MembershipController {

    private final MembershipService membershipService;

    /**
     * 스페이스 멤버 추가
     * 프런트: dataProvider.create(
     * `workspaces/${wsId}/spaces/${spaceId}/members`,
     * { data: { email, role, note } }
     * )
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/workspaces/{workspaceId}/spaces/{spaceId}/members")
    public ResponseEntity<List<MemberResponse>> addSpaceAndProjectMember(@PathVariable Long workspaceId,
                                                         @PathVariable Long spaceId,
                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                         @RequestBody AddSpaceAndProjectMembersRequest request) {

        return ResponseEntity.ok(membershipService.addSpaceAndProjectMembers(customUserDetails.getId(), workspaceId, spaceId, request.projectIds(), request.reqList()));
    }

    @GetMapping("/workspaces/{workspaceId}/members")
    public ResponseEntity<List<UserInfoResponse>> getWorkspaceMembers(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(membershipService.getWorkspaceMembers(workspaceId));
    }

    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<List<UserInfoResponse>> getProjectMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(membershipService.getProjectMembers(projectId));
    }
}
