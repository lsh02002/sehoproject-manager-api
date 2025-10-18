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
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMemberRepository;
import com.sehoprojectmanagerapi.service.exceptions.AccessDeniedException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.AddMemberRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.privilege.MemberResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final EntityManager em;

    private static final Set<WorkspaceRole> CAN_GRANT = EnumSet.of(WorkspaceRole.OWNER, WorkspaceRole.ADMIN);

    private void ensureGranterCanGrant(Long granterUserId, Long workspaceId) {
        WorkspaceRole role = workspaceMemberRepository.findRole(workspaceId, granterUserId)
                .orElseThrow(() -> new AccessDeniedException("워크스페이스 멤버가 아님", null));
        if (!CAN_GRANT.contains(role)) {
            throw new AccessDeniedException("권한 부여 권한이 없음", null);
        }
    }

    private User resolveTargetUserInWorkspace(Long workspaceId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("대상 유저를 찾을 수 없음: " + email));
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
            throw new IllegalArgumentException("대상 유저가 해당 워크스페이스 멤버가 아님");
        }
        return user;
    }

    @Transactional
    public MemberResponse addSpaceMember(Long granterUserId,
                                         Long workspaceId,
                                         Long spaceId,
                                         AddMemberRequest req) {
        ensureGranterCanGrant(granterUserId, workspaceId);

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스 없음", null));
        if (!Objects.equals(space.getWorkspace().getId(), workspaceId)) {
            throw new IllegalArgumentException("스페이스가 워크스페이스에 속하지 않음");
        }

        User target = resolveTargetUserInWorkspace(workspaceId, req.email());

        if (spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), target.getId())) {
            // 이미 멤버면 idempotent 처리: 그냥 성공처럼 응답하거나 409 반환(정책 선택)
            SpaceMember existing = em.createQuery("""
                select sm from SpaceMember sm
                where sm.space.id = :sid and sm.user.id = :uid
            """, SpaceMember.class)
                    .setParameter("sid", space.getId()).setParameter("uid", target.getId())
                    .getSingleResult();
            return new MemberResponse(existing.getId(), target.getId(), space.getId(), "SPACE", existing.getRole().name(), null);
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
            ProjectMember existing = em.createQuery("""
                select pm from ProjectMember pm
                where pm.project.id = :pid and pm.user.id = :uid
            """, ProjectMember.class)
                    .setParameter("pid", projectId).setParameter("uid", target.getId())
                    .getSingleResult();
            return new MemberResponse(existing.getId().getProjectId(), target.getId(), projectId, "PROJECT", null, existing.getRole().name());
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
}
