package com.sehoprojectmanagerapi.service.membership;

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
import com.sehoprojectmanagerapi.service.exceptions.AccessDeniedException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.user.UserInfoResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.AddMemberRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.MemberResponse;
import com.sehoprojectmanagerapi.web.mapper.UserMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @Transactional
    public MemberResponse addSpaceMember(Long granterUserId,
                                         Long workspaceId,
                                         Long spaceId,
                                         AddMemberRequest req) {
        ensureGranterCanGrant(granterUserId, workspaceId);

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스 없음", null));
        if (!Objects.equals(space.getWorkspace().getId(), workspaceId)) {
            throw new NotAcceptableException("스페이스가 워크스페이스에 속하지 않음", null);
        }

        User target = resolveTargetUserInWorkspace(workspaceId, req.email());

        if (spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), target.getId())) {
            throw new NotAcceptableException("이미 멤버인 스페이스 사용자입니다.", target.getId());
        }

        SpaceMember sm = new SpaceMember();
        sm.setSpace(space);
        sm.setUser(target);
        sm.setRole(SpaceRole.valueOf(req.requestRole())); // 유효성 검사 필요시 try-catch
        sm.setJoinedAt(OffsetDateTime.now());
//        sm.setGrantedBy(em.getReference(User.class, granterUserId));
//        sm.setNote(req.note());
        spaceMemberRepository.save(sm);

        return new MemberResponse(sm.getId(), target.getId(), space.getId(), "SPACE", sm.getRole().name(), null);
    }

    @Transactional
    public MemberResponse addProjectMember(Long granterUserId,
                                           Long projectId,
                                           AddMemberRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("프로젝트 없음", null));

        Long workspaceId = project.getSpace().getWorkspace().getId();
        ensureGranterCanGrant(granterUserId, workspaceId);

        User target = resolveTargetUserInWorkspace(workspaceId, req.email());

        if (projectMemberRepository.existsByUserIdAndProjectId(target.getId(), projectId)) {
            throw new NotAcceptableException("이미 멤버인 프로젝트 사용자입니다.", target.getId());
        }

        ProjectMember pm = new ProjectMember();
        pm.setProject(project);
        pm.setUser(target);
        pm.setRole(RoleProject.valueOf(req.requestRole())); // 프로젝트 역할 Enum 별도라면 바꾸세요
        pm.setJoinedAt(OffsetDateTime.now());
//        pm.setGrantedBy(em.getReference(User.class, granterUserId));
//        pm.setNote(req.note());
        projectMemberRepository.save(pm);

        return new MemberResponse(pm.getId().getProjectId(), target.getId(), projectId, "PROJECT", null, pm.getRole().name());
    }

    @Transactional
    public List<UserInfoResponse> getWorkspaceMembers(Long workspaceId) {
        return workspaceMemberRepository.findByWorkspaceId(workspaceId)
                .stream().map(WorkspaceMember::getUser)
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
