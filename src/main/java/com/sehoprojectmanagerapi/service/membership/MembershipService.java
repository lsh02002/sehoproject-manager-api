package com.sehoprojectmanagerapi.service.membership;

import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.repository.space.SpaceRole;
import com.sehoprojectmanagerapi.repository.space.spacemember.SpaceMember;
import com.sehoprojectmanagerapi.repository.space.spacemember.SpaceMemberRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMember;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMemberRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.AccessDeniedException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.user.UserInfoResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.AddMemberRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.MemberResponse;
import com.sehoprojectmanagerapi.web.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private static final Set<WorkspaceRole> CAN_GRANT = EnumSet.of(WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ActivityLogService activityLogService;
    private final SnapshotFunc snapshotFunc;

    @Transactional
    public List<MemberResponse> addSpaceAndProjectMembers(Long granterUserId,
                                                          Long workspaceId,
                                                          Long spaceId,
                                                          List<Long> projectIds,
                                                          List<AddMemberRequest> reqList) {

        ensureGranterCanGrant(granterUserId, workspaceId);

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스 없음", null));

        if (!Objects.equals(space.getWorkspace().getId(), workspaceId)) {
            throw new NotAcceptableException("스페이스가 워크스페이스에 속하지 않음", null);
        }

        if (reqList == null || reqList.isEmpty()) {
            throw new NotAcceptableException("대상 유저 정보가 비어있습니다", null);
        }

        if (projectIds == null || projectIds.isEmpty()) {
            throw new NotAcceptableException("프로젝트 목록이 비어 있음", null);
        }

        List<Project> projects = projectRepository.findAllById(projectIds);

        if (projects.size() != projectIds.size()) {
            throw new NotFoundException("존재하지 않는 프로젝트가 포함되어 있음", null);
        }

        for (Project project : projects) {
            if (!Objects.equals(project.getSpace().getId(), spaceId)) {
                throw new NotAcceptableException(
                        "프로젝트가 해당 스페이스에 속하지 않음. projectId=" + project.getId(),
                        null
                );
            }
        }

        List<MemberResponse> responses = new ArrayList<>();

        for (AddMemberRequest req : reqList) {
            User target = resolveTargetUserInWorkspace(workspaceId, req.email());

            // 1. SpaceMember 추가
            if (spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, target.getId())) {
                throw new NotAcceptableException("이미 스페이스 권한이 부여되어 있습니다! " + target.getEmail(), null);
            }

            SpaceMember sm = new SpaceMember();
            sm.setSpace(space);
            sm.setUser(target);
            sm.setRole(SpaceRole.valueOf(req.requestRole()));
            sm.setJoinedAt(LocalDateTime.now());

            sm = spaceMemberRepository.save(sm);

            Object afterSpaceMember = snapshotFunc.snapshot(sm);

            activityLogService.log(
                    ActivityEntityType.SPACE_MEMBER,
                    ActivityAction.CREATE,
                    sm.getId(),
                    sm.logMessage(),
                    target,
                    null,
                    afterSpaceMember
            );

            responses.add(new MemberResponse(
                    sm.getId(),
                    target.getId(),
                    space.getId(),
                    "SPACE",
                    sm.getRole().name(),
                    null
            ));

            // 2. 여러 프로젝트에 ProjectMember 추가
            for (Project project : projects) {
                if (projectMemberRepository.existsByUserIdAndProjectId(target.getId(), project.getId())) {
                    throw new NotAcceptableException(
                            "이미 프로젝트의 멤버입니다. email=" + req.email() + ", projectId=" + project.getId(),
                            null
                    );
                }

                ProjectMember pm = new ProjectMember();
                pm.setProject(project);
                pm.setUser(target);
                pm.setRole(RoleProject.valueOf(req.roleProject()));
                pm.setJoinedAt(LocalDateTime.now());

                pm = projectMemberRepository.save(pm);

                Object afterProjectMember = snapshotFunc.snapshot(pm);

                activityLogService.log(
                        ActivityEntityType.PROJECT_MEMBER,
                        ActivityAction.CREATE,
                        pm.getId(),
                        pm.logMessage(),
                        target,
                        null,
                        afterProjectMember
                );

                responses.add(new MemberResponse(
                        pm.getId(),
                        target.getId(),
                        project.getId(),
                        "PROJECT",
                        null,
                        pm.getRole().name()
                ));
            }
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<UserInfoResponse> getWorkspaceMembersNotInSpace(Long workspaceId, Long spaceId) {
        return userRepository.findWorkspaceMembersNotInSpace(workspaceId, spaceId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<UserInfoResponse> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId)
                .stream().map(ProjectMember::getUser)
                .map(userMapper::toResponse).toList();
    }

    private void ensureGranterCanGrant(Long granterUserId, Long workspaceId) {
        WorkspaceRole role = workspaceMemberRepository.findRole(workspaceId, granterUserId)
                .orElseThrow(() -> new AccessDeniedException("워크스페이스 멤버가 아님", null));
        if (!CAN_GRANT.contains(role)) {
            throw new NotAcceptableException("권한 부여 권한이 없음", null);
        }
    }

    private User resolveTargetUserInWorkspace(Long workspaceId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("대상 유저를 찾을 수 없음:", email));
        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(user.getId(), workspaceId)) {
            throw new NotAcceptableException("대상 유저가 해당 워크스페이스 멤버가 아님", user.getId());
        }
        return user;
    }
}
