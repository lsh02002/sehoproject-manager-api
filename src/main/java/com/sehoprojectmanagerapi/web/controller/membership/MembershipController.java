package com.sehoprojectmanagerapi.web.controller.membership;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.membership.MembershipService;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.AddMemberRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.BatchAddMembersRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public ResponseEntity<MemberResponse> addSpaceMember(@PathVariable Long workspaceId,
                                                         @PathVariable Long spaceId,
                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                         @RequestBody AddMemberRequest req) {
        // me.getUserId()는 인증 객체에서 현재 사용자(부여자) ID를 가져온다고 가정
        return ResponseEntity.ok(membershipService.addSpaceMember(customUserDetails.getId(), workspaceId, spaceId, req));
    }

    /**
     * 프로젝트 멤버 추가
     * 프런트: dataProvider.create(
     * `projects/${projectId}/members`,
     * { data: { email, role, note } }
     * )
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/projects/{projectId}/members")
    public ResponseEntity<MemberResponse> addProjectMember(@PathVariable Long projectId,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                           @RequestBody AddMemberRequest req) {
        return ResponseEntity.ok(membershipService.addProjectMember(customUserDetails.getId(), projectId, req));
    }

    /**
     * (옵션) 벌크 멤버 추가
     * 프런트에서 여러 spaceIds / projectIds를 선택해 한 번에 부여하려면 사용
     * <p>
     * 예시 요청 바디:
     * {
     * "email": "user@example.com",
     * "role": "VIEWER",
     * "note": "",
     * "spaceIds": [11, 12],
     * "projectIds": [101, 102]
     * }
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/workspaces/{workspaceId}/members:batch")
    public ResponseEntity<List<MemberResponse>> grantBatch(@PathVariable Long workspaceId,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                           @RequestBody BatchAddMembersRequest req) {
        final Long granterId = customUserDetails.getId();

        final List<MemberResponse> result = new ArrayList<>();

        // spaceIds 처리
        if (req.spaceIds() != null) {
            for (Long spaceId : req.spaceIds()) {
                var res = membershipService.addSpaceMember(
                        granterId, workspaceId, spaceId,
                        new AddMemberRequest(req.email(), req.requestRole(), null, req.note())
                );
                result.add(res);
            }
        }

        // projectIds 처리 (project → workspace 일치 여부는 service에서 검증)
        if (req.projectIds() != null) {
            for (Long projectId : req.projectIds()) {
                var res = membershipService.addProjectMember(
                        granterId, projectId,
                        new AddMemberRequest(req.email(), null, req.roleProject(), req.note())
                );
                result.add(res);
            }
        }
        return ResponseEntity.ok(result);
    }
}
