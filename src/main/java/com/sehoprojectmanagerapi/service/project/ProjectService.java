package com.sehoprojectmanagerapi.service.project;

import com.sehoprojectmanagerapi.config.rolefunction.RoleFunc;
import com.sehoprojectmanagerapi.repository.common.CommonStatus;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.project.projectinvite.ProjectInvite;
import com.sehoprojectmanagerapi.repository.project.projectinvite.ProjectInviteRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.project.ProjectInviteRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectInviteResponse;
import com.sehoprojectmanagerapi.web.dto.project.ProjectRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import com.sehoprojectmanagerapi.web.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import static com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject.MANAGER;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final int DEFAULT_INVITE_TTL_DAYS = 14;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectInviteRepository projectInviteRepository;
    private final ProjectMapper projectMapper;
    private final RoleFunc roleFunc;
    private final SpaceRepository spaceRepository;

    @Transactional
    public List<ProjectResponse> getAllTeamsByUser(Long userId) {
        return projectMemberRepository.findByUserId(userId)
                .stream().map(projectMember -> projectMapper.toProjectResponse(projectMember.getProject())).toList();
    }

    @Transactional
    public List<ProjectResponse> getAllProjectsByUserAndSpace(Long userId, Long spaceId) {
        return projectMemberRepository.findByUserId(userId)
                .stream().filter(projectMember -> Objects.equals(projectMember.getProject().getSpace().getId(), spaceId))
                .map(projectMember -> projectMapper.toProjectResponse(projectMember.getProject())).toList();
    }

    @Transactional
    public ProjectResponse getProjectById(Long userId, Long projectId) {
        return projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .map(projectMember -> projectMapper.toProjectResponse(projectMember.getProject()))
                .orElseThrow(() -> new NotFoundException("해당 프로젝트 접근 권한이 없습니다.", null));
    }

    @Transactional
    public ProjectResponse createProject(Long userId, ProjectRequest projectRequest) {
        if (projectRequest.getName() == null || projectRequest.getName().trim().isEmpty()) {
            throw new BadRequestException("프로젝트명이 비어있습니다.", null);
        }

        Space space = spaceRepository.findById(projectRequest.getSpaceId())
                .orElseThrow(() -> new NotFoundException("해당 스페이스를 찾을 수 없습니다.", projectRequest.getSpaceId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        Project project = Project.builder()
                .space(space)
                .key(projectRequest.getProjectKey())
                .name(projectRequest.getName())
                .description(projectRequest.getDescription())
                .status(CommonStatus.ACTIVE)
                .startDate(projectRequest.getStartDate())
                .dueDate(projectRequest.getDueDate())
                .createdBy(user)
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(savedProject);
        projectMember.setUser(user);
        projectMember.setRole(MANAGER);
        projectMember.setJoinedAt(OffsetDateTime.now());

        projectMemberRepository.save(projectMember);

        return projectMapper.toProjectResponse(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest projectRequest) {
        System.out.println("tx readOnly? {}" + TransactionSynchronizationManager.isCurrentTransactionReadOnly());

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotFoundException("해당 팀에 본 사용자는 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("프로젝트 수정 권한이 없습니다.", userId);
        }

        Project project = projectMember.getProject();

        // 문자열 필드: null이면 유지, ""이면 null로 지우기
        if (projectRequest.getName() != null) {
            project.setName(projectRequest.getName().trim().isEmpty() ? null : projectRequest.getName());
        }

        if (projectRequest.getDescription() != null) {
            project.setDescription(projectRequest.getDescription().trim().isEmpty() ? null : projectRequest.getDescription());
        }

        // 날짜 필드: 값이 왔으면 그대로 반영
        if (projectRequest.getStartDate() != null) {
            project.setStartDate(projectRequest.getStartDate());
        }

        if (projectRequest.getDueDate() != null) {
            project.setDueDate(projectRequest.getDueDate());
        }

        // 작성자 변경: creatorId가 있을 때만 반영
        if (projectRequest.getCreatorId() != null) {
            if (projectRequest.getCreatorId() > 0) {
                project.setCreatedBy(
                        userRepository.findById(projectRequest.getCreatorId())
                                .orElseThrow(() -> new NotFoundException("해당 작성자를 찾을 수 없습니다.", projectRequest.getCreatorId()))
                );
            } else {
                project.setCreatedBy(null); // 0 이하이면 작성자 제거
            }
        }

        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(savedProject);
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        try {
            projectMemberRepository.deleteByUserIdAndProjectId(userId, projectId);
            projectRepository.deleteById(projectId);
        } catch (RuntimeException e) {
            throw new ConflictException("해당 프로젝트 삭제에 실패했습니다.", projectId);
        }
    }

    @Transactional
    public ProjectInviteResponse inviteToProject(Long inviterId, Long projectId, ProjectInviteRequest request) {
        // 1) 필수 로드
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", projectId));
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new NotFoundException("초대한 주인을 찾을 수 없습니다.", inviterId));
        User invited = userRepository.findById(request.invitedUserId())
                .orElseThrow(() -> new NotFoundException("초대된 손님을 찾을 수 없습니다.", request.invitedUserId()));

        // 2) 권한 체크 (프로젝트에 초대할 권한이 있는지)
        if (!projectMemberRepository.existsByUserIdAndProjectId(inviterId, project.getId())) {
            throw new NotAcceptableException("프로젝트에 초대할 권한이 없습니다.", projectId);
        }

        // 3) 자기 자신 초대 방지
        if (inviter.getId().equals(invited.getId())) {
            throw new BadRequestException("자기 자신을 초대할 수 없습니다.", projectId);
        }

        // 4) 이미 멤버인지 검사
        boolean alreadyMember = projectMemberRepository.existsByUserIdAndProjectId(invited.getId(), projectId);
        if (alreadyMember) {
            throw new ConflictException("이미 초대된 사용자입니다.", projectId);
        }

        // 5) 중복/유효 초대 존재 여부 검사 (PENDING && 미만료)
        boolean hasPending = projectInviteRepository.existsByProjectIdAndInvitedUserIdAndStatusInAndExpiresAtAfter(
                projectId, invited.getId(),
                List.of(ProjectInvite.Status.PENDING),
                OffsetDateTime.now()
        );
        if (hasPending) {
            throw new ConflictException("초대된 내용이 이미 있습니다.", projectId);
        }

        // 6) 초대 엔티티 생성
        ProjectInvite invite = new ProjectInvite();
        invite.setProject(project);
        invite.setInviter(inviter);
        invite.setInvitedUser(invited);
        invite.setStatus(ProjectInvite.Status.PENDING);
        invite.setMessage(request.message()); // 선택
        invite.setRequestedRole(request.requestedRole()); // 선택(OWNER/MANAGER/MEMBER 등)
        invite.setExpiresAt(OffsetDateTime.now().plusDays(DEFAULT_INVITE_TTL_DAYS));

        // 7) 저장
        ProjectInvite saved = projectInviteRepository.save(invite);

        // 8) (옵션) 알림/이벤트 발행
        // domainEvents.publish(new ProjectInviteCreatedEvent(saved.getId()));
        // notificationService.notifyUser(invited.getId(), ...);

        // 9) 응답 매핑
        return projectMapper.toInviteResponse(saved);
    }

    @Transactional
    public ProjectResponse acceptInvite(Long userId, Long projectId, Long inviteId) {
        // 1) 초대/프로젝트/유저 로드
        ProjectInvite invite = projectInviteRepository.findByIdAndProjectId(inviteId, projectId)
                .orElseThrow(() -> new NotFoundException("해당 초대 내역이 없습니다.", inviteId));
        Project project = invite.getProject();

        // 2) 수락자 본인 여부
        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new NotAcceptableException("당신은 초대되지 않았습니다.", projectId);
        }

        // 3) 상태/만료 검사
        if (invite.getStatus() != ProjectInvite.Status.PENDING) {
            throw new ConflictException("초대 기간이 만료되었습니다.", projectId);
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(OffsetDateTime.now())) {
            invite.setStatus(ProjectInvite.Status.EXPIRED);
            projectInviteRepository.save(invite);
            throw new ConflictException("초대 기간이 만료되었습니다.", projectId);
        }

        // 4) 이미 멤버인지 검사 (외부에서 누군가 선점 추가했을 수 있음)
        boolean alreadyMember = projectMemberRepository
                .existsByUserIdAndProjectId(userId, projectId);
        if (alreadyMember) {
            // 멤버면 초대를 수락 완료 처리만 하고 반환
            invite.setStatus(ProjectInvite.Status.ACCEPTED);
            projectInviteRepository.save(invite);
            // 같은 사용자에 대한 다른 PENDING 초대 무효화(선택)
            projectInviteRepository.expireOtherPendings(projectId, userId, invite.getId(), OffsetDateTime.now());
            return projectMapper.toProjectResponse(project);
        }

        // 5) 멤버 추가 (요청된 역할이 없으면 MEMBER 기본)
        RoleProject role = invite.getRequestedRole() != null ? invite.getRequestedRole() : RoleProject.CONTRIBUTOR;
        ProjectMember newMember = new ProjectMember();
        newMember.setProject(project);
        newMember.setUser(invite.getInvitedUser());
        newMember.setRole(role);
        projectMemberRepository.save(newMember);

        // 6) 초대 상태 갱신 + 동일 사용자 다른 초대 무효화
        invite.setStatus(ProjectInvite.Status.ACCEPTED);
        projectInviteRepository.save(invite);
        projectInviteRepository.expireOtherPendings(projectId, userId, invite.getId(), OffsetDateTime.now());

        // 7) (옵션) 이벤트/알림 발행
        // domainEvents.publish(new ProjectMemberJoinedEvent(project.getId(), userId, role));

        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    public void declineInvite(Long userId, Long projectId, Long inviteId) {
        ProjectInvite invite = projectInviteRepository.findByIdAndProjectId(inviteId, projectId)
                .orElseThrow(() -> new NotFoundException("초대 내역이 없습니다.", inviteId));

        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new NotAcceptableException("당신은 초대되지 않았습니다.", userId);
        }

        if (invite.getStatus() != ProjectInvite.Status.PENDING) {
            // 이미 처리된 초대는 멱등적으로 무시
            return;
        }

        // 만료 상태로 바꾸고 싶다면 아래 2줄 중 택1 (DECLINED 권장)
        invite.setStatus(ProjectInvite.Status.DECLINED);
        // invite.setStatus(ProjectInvite.Status.EXPIRED);

        projectInviteRepository.save(invite);
        // (옵션) 대체 초대를 위해 알림/이벤트 발행 가능
        // notificationService.notifyUser(invite.getInviter().getId(), ...);
    }
}
