package com.sehoprojectmanagerapi.service.workspace;

import com.sehoprojectmanagerapi.config.function.RoleFunc;
import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
import com.sehoprojectmanagerapi.config.security.SecurityUtil;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.common.Role;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRepository;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import com.sehoprojectmanagerapi.repository.workspace.workspaceinvite.WorkspaceInvite;
import com.sehoprojectmanagerapi.repository.workspace.workspaceinvite.WorkspaceInviteRepository;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMember;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMemberRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.service.project.ProjectService;
import com.sehoprojectmanagerapi.service.space.SpaceService;
import com.sehoprojectmanagerapi.service.task.TaskService;
import com.sehoprojectmanagerapi.web.dto.project.ProjectRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import com.sehoprojectmanagerapi.web.dto.space.SpaceRequest;
import com.sehoprojectmanagerapi.web.dto.space.SpaceResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.TreeRow;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceResponse;
import com.sehoprojectmanagerapi.web.dto.workspace.invite.WorkspaceInviteRequest;
import com.sehoprojectmanagerapi.web.dto.workspace.invite.WorkspaceInviteResponse;
import com.sehoprojectmanagerapi.web.mapper.user.UserMapper;
import com.sehoprojectmanagerapi.web.mapper.workspace.WorkspaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private static final int DEFAULT_INVITE_TTL_DAYS = 14;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceInviteRepository workspaceInviteRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final WorkspaceMapper workspaceMapper;

    private final SpaceService spaceService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final RoleFunc roleFunc;
    private final UserMapper userMapper;
    private final ActivityLogService activityLogService;
    private final SnapshotFunc snapshotFunc;

    public List<TreeRow> getTreeRowsForCurrentUser(Long userId, Long workspaceId) {

        // 2️⃣ 접근 가능한 항목만 반환
        return spaceRepository.findTreeRowsVisibleToUser(
                workspaceId,
                userId,
                Role.rolesGrantingSpaceVisibility(),
                Role.rolesGrantingProjectVisibility()
        );
    }

    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, WorkspaceRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", userId));

        if (request.name() == null || request.name().isEmpty()) {
            throw new ConflictException("이름란이 비어있습니다.", null);
        }

        if (workspaceRepository.existsBySlug(request.slug())) {
            throw new ConflictException("중복된 워크스페이스 슬러그입니다.", request.slug());
        }

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .slug(request.slug())
                .createdBy(creator)
                .build();
        workspace = workspaceRepository.save(workspace);

        Object afterworkspace = snapshotFunc.snapshot(workspace);

        // 작성자 = OWNER 로 멤버십 자동 생성
        WorkspaceMember owner = WorkspaceMember.builder()
                .workspace(workspace)
                .user(creator)
                .role(WorkspaceRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        workspaceMemberRepository.save(owner);

        Object afterworkspacemember = snapshotFunc.snapshot(owner);

        activityLogService.log(ActivityEntityType.WORKSPACE_MEMBER, ActivityAction.CREATE, owner.getId(), owner.logMessage(), creator, null, afterworkspacemember);

        activityLogService.log(ActivityEntityType.WORKSPACE, ActivityAction.CREATE, workspace.getId(), workspace.logMessage(), creator, null, afterworkspace);

        WorkspaceResponse response = workspaceMapper.toResponse(workspace);

        createSample(userId, request, response);

        return response;
    }

    @Transactional
    public List<WorkspaceResponse> listWorkspaces(Long userId) {
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByUserId(userId);

        return workspaceMembers
                .stream().map(member -> workspaceMapper.toResponse(member.getWorkspace())).toList();
    }

    @Transactional
    public WorkspaceResponse getWorkspace(Long userId, Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(userId, id)) {
            throw new NotAcceptableException("워크스페이스 멤버만 조회할 수 있습니다.", null);
        }

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long userId, Long id, WorkspaceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", userId));

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        Object beforeworkspace = snapshotFunc.snapshot(workspace);

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(userId, id)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스 멤버만 수정할 수 있습니다.", null));
        if (!(role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN)) {
            throw new NotAcceptableException("OWNER 또는 ADMIN만 수정할 수 있습니다.", null);
        }

        if (request.name() == null || request.name().isEmpty()) {
            throw new ConflictException("이름란이 비어있습니다.", null);
        }

        if (!workspace.getSlug().equals(request.slug()) && workspaceRepository.existsBySlug(request.slug())) {
            throw new ConflictException("중복된 워크스페이스 슬러그입니다.", request.slug());
        }

        workspace.setName(request.name());
        workspace.setSlug(request.slug());

        Object afterworkspace = snapshotFunc.snapshot(workspace);

        activityLogService.log(
                ActivityEntityType.WORKSPACE, ActivityAction.UPDATE,
                workspace.getId(), workspace.logMessage(),
                user,
                beforeworkspace, afterworkspace
        );

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public void deleteWorkspace(Long currentUserId, Long id) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new NotFoundException("해당 유저를 찾을 수 없습니다.", currentUserId));

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        Object beforeworkspace = snapshotFunc.snapshot(workspace);

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(currentUserId, id)
                .orElseThrow(() -> new BadCredentialsException("워크스페이스 멤버만 삭제할 수 있습니다.", null));
        if (role != WorkspaceRole.OWNER) {
            throw new BadCredentialsException("OWNER만 워크스페이스를 삭제할 수 있습니다.", null);
        }

        activityLogService.log(ActivityEntityType.WORKSPACE, ActivityAction.DELETE, workspace.getId(), workspace.logMessage(), user, beforeworkspace, null);

        workspaceRepository.delete(workspace);
    }

    @Transactional
    public List<WorkspaceInviteResponse> getMyWorkspaceInvites(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        List<WorkspaceInvite> invites = workspaceInviteRepository.findAllByInvitedUserId(userId);

        return invites.stream()
                .map(workspaceMapper::toInviteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<WorkspaceInviteResponse> inviteToWorkspace(Long inviterId, List<WorkspaceInviteRequest> requests) {
        // 0) 요청값 검증
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("초대 요청이 비어 있습니다.", null);
        }

        // 같은 워크스페이스만 허용
        Long workspaceId = requests.get(0).workspaceId();
        boolean hasDifferentWorkspaceId = requests.stream()
                .anyMatch(req -> !workspaceId.equals(req.workspaceId()));

        if (hasDifferentWorkspaceId) {
            throw new BadRequestException("한 번의 요청에서는 동일한 워크스페이스만 초대할 수 있습니다.", null);
        }

        // 같은 유저 중복 초대 방지 (요청 내부 중복)
        List<String> invitedUserEmails = new ArrayList<>();
        for(WorkspaceInviteRequest request: requests) {
            invitedUserEmails.add(request.invitedUserEmail());
        }

        // 1) 공통 엔티티 로드
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("해당 워크스페이스를 찾을 수 없습니다.", workspaceId));

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new NotFoundException("초대한 사용자를 찾을 수 없습니다.", inviterId));

        // 2) 권한 체크
        WorkspaceRole inviterRole = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(inviterId, workspaceId)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스에 초대할 권한이 없습니다.", workspaceId));

        if (!roleFunc.hasAtLeast(inviterRole, WorkspaceRole.ADMIN)) {
            throw new NotAcceptableException("워크스페이스에 초대할 권한이 없습니다.", workspaceId);
        }

        // 3) 초대 대상 유저들 조회
        List<User> invitedUsers = userRepository.findAllByEmailIn(invitedUserEmails);
        Map<String, User> invitedUserMap = invitedUsers.stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));

        if(invitedUserEmails.isEmpty()) {
            throw new NotFoundException("이메일란이 비어있습니다", null);
        }

        // 존재하지 않는 유저 체크
        for (String invitedUserEmail : invitedUserEmails) {
            if (!invitedUserMap.containsKey(invitedUserEmail)) {
                throw new NotFoundException("초대된 사용자를 찾을 수 없습니다.", invitedUserEmail);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<WorkspaceInvite> invitesToSave = new ArrayList<>();

        // 4) 요청별 검증 + 엔티티 생성
        for (WorkspaceInviteRequest req : requests) {
            User invited = invitedUserMap.get(req.invitedUserEmail());

            // 자기 자신 초대 방지
            if (inviter.getId().equals(invited.getId())) {
                throw new BadRequestException("자기 자신을 초대할 수 없습니다.", invited.getId());
            }

            // 이미 멤버인지 검사
            boolean alreadyMember = workspaceMemberRepository.existsByUserIdAndWorkspaceId(invited.getId(), workspaceId);
            if (alreadyMember) {
                throw new ConflictException("이미 워크스페이스 멤버입니다. " + invited.getEmail(), null);
            }

            // 진행 중인 초대 존재 여부
            boolean hasPending = workspaceInviteRepository
                    .existsByWorkspaceIdAndInvitedUserIdAndStatusInAndExpiresAtAfter(
                            workspaceId,
                            invited.getId(),
                            List.of(WorkspaceInvite.Status.PENDING),
                            now
                    );

            if (hasPending) {
                throw new ConflictException("진행 중인 초대가 이미 있습니다.", invited.getId());
            }

            WorkspaceInvite invite = new WorkspaceInvite();
            invite.setWorkspace(workspace);
            invite.setInviter(inviter);
            invite.setInvitedUser(invited);
            invite.setStatus(WorkspaceInvite.Status.PENDING);
            invite.setMessage(req.message());
            invite.setRequestedRole(
                    req.requestedRole() != null ? req.requestedRole() : WorkspaceRole.MEMBER
            );
            invite.setExpiresAt(now.plusDays(DEFAULT_INVITE_TTL_DAYS));

            invitesToSave.add(invite);
        }

        // 5) 저장
        List<WorkspaceInvite> savedInvites = workspaceInviteRepository.saveAll(invitesToSave);

        // 6) (옵션) 이벤트/알림 발행
        // savedInvites.forEach(invite -> domainEvents.publish(new WorkspaceInviteCreatedEvent(invite.getId())));

        // 7) 응답
        return savedInvites.stream()
                .map(workspaceMapper::toInviteResponse)
                .toList();
    }

    @Transactional
    public WorkspaceResponse acceptWorkspaceInvite(Long userId, Long workspaceId, Long inviteId) {
        // 1) 초대/워크스페이스/유저 로드
        WorkspaceInvite invite = workspaceInviteRepository.findByIdWithWorkspace(inviteId, workspaceId)
                .orElseThrow(() -> new NotFoundException("해당 초대 내역이 없습니다.", inviteId));
        Workspace workspace = invite.getWorkspace();

        // 2) 수락자 본인 여부
        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new NotAcceptableException("당신은 초대되지 않았습니다.", workspaceId);
        }

        // 3) 상태/만료 검사
        if (invite.getStatus() != WorkspaceInvite.Status.PENDING) {
            throw new ConflictException("이미 처리되었거나 만료된 초대입니다.", workspaceId);
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(WorkspaceInvite.Status.EXPIRED);
            workspaceInviteRepository.save(invite);
            throw new ConflictException("초대 기간이 만료되었습니다.", workspaceId);
        }

        // 4) 이미 워크스페이스 멤버인지 검사 (외부에서 선점 추가 가능)
        boolean alreadyMember = workspaceMemberRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);
        if (alreadyMember) {
            invite.setStatus(WorkspaceInvite.Status.ACCEPTED);
            workspaceInviteRepository.save(invite);
            // 같은 사용자에 대한 다른 PENDING 초대 무효화(선택)
            workspaceInviteRepository.expireOtherPendings(workspaceId, userId, invite.getId(), LocalDateTime.now());
            return workspaceMapper.toResponse(workspace);
        }

        // 5) 멤버 추가 (요청된 역할 없으면 기본값)
        WorkspaceRole role = invite.getRequestedRole() != null
                ? invite.getRequestedRole()
                : WorkspaceRole.MEMBER; // 필요시 기본 롤 상수 변경

        WorkspaceMember newMember = new WorkspaceMember();
        newMember.setWorkspace(workspace);
        newMember.setUser(invite.getInvitedUser());
        newMember.setRole(role);
        newMember.setJoinedAt(LocalDateTime.now());

        workspaceMemberRepository.save(newMember);

        // 6) 초대 상태 갱신 + 동일 사용자 다른 초대 무효화
        invite.setStatus(WorkspaceInvite.Status.ACCEPTED);
        workspaceInviteRepository.save(invite);
        workspaceInviteRepository.expireOtherPendings(workspaceId, userId, invite.getId(), LocalDateTime.now());

        // 7) (옵션) 이벤트/알림 발행
        // domainEvents.publish(new WorkspaceMemberJoinedEvent(workspace.getId(), userId, role));

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse declineWorkspaceInvite(Long userId, Long workspaceId, Long inviteId) {
        WorkspaceInvite invite = workspaceInviteRepository.findByIdAndWorkspaceId(inviteId, workspaceId)
                .orElseThrow(() -> new NotFoundException("초대 내역이 없습니다.", inviteId));

        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new NotAcceptableException("당신은 초대되지 않았습니다.", userId);
        }

        if (invite.getStatus() != WorkspaceInvite.Status.PENDING) {
            // 이미 처리된 초대는 멱등적으로 무시 (null 반환 or 현재 워크스페이스 응답)
            return workspaceMapper.toResponse(invite.getWorkspace());
        }

        // 상태 전환: DECLINED 권장
        invite.setStatus(WorkspaceInvite.Status.DECLINED);
        workspaceInviteRepository.save(invite);

        return workspaceMapper.toResponse(invite.getWorkspace());
    }

    @Transactional
    public List<WorkspaceInviteResponse> getGivePrivileges(Long userId) {
        return workspaceInviteRepository.findByInviterId(userId)
                .stream().filter(invited -> invited.getStatus() == WorkspaceInvite.Status.ACCEPTED)
                .map(workspaceMapper::toInviteResponse).toList();
    }

    @Transactional
    public void createSample(Long userId, WorkspaceRequest workspaceRequest, WorkspaceResponse workspaceResponse) {
        //WorkspaceResponse workspaceResponse = workspaceService.createWorkspace(userId, workspaceRequest);

        SpaceRequest spaceRequest = SpaceRequest.builder()
                .name("Team Space")
                .slug("team-space")
                .build();

        SpaceResponse spaceResponse = spaceService.createSpace(userId, workspaceResponse.id(), spaceRequest);

        ProjectRequest projectRequest = ProjectRequest.builder()
                .spaceId(spaceResponse.id())
                .projectKey("BACKEND")
                .name("Project1")
                .description("This is a test project1")
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now())
                .creatorId(userId)
                .build();

        ProjectResponse projectResponse = projectService.createProject(userId, projectRequest);

        TaskRequest taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getId())
                .name("Task1")
                .description("This is a test task1")
                .assignees(new ArrayList<>())
                .sprintId(null)
                .milestoneId(null)
                .tags(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .state("TODO")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getId())
                .name("Task2")
                .description("This is a test task2")
                .assignees(new ArrayList<>())
                .sprintId(null)
                .milestoneId(null)
                .tags(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .state("TODO")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getId())
                .name("Task3")
                .description("This is a test task3")
                .assignees(new ArrayList<>())
                .sprintId(null)
                .milestoneId(null)
                .tags(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .state("TODO")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        ProjectRequest projectRequest2 = ProjectRequest.builder()
                .spaceId(spaceResponse.id())
                .projectKey("FRONTEND")
                .name("Project2")
                .description("This is a test project2")
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now())
                .creatorId(userId)
                .build();

        ProjectResponse projectResponse2 = projectService.createProject(userId, projectRequest2);

        TaskRequest taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getId())
                .name("Task1")
                .description("This is a test task1")
                .assignees(new ArrayList<>())
                .sprintId(null)
                .milestoneId(null)
                .tags(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .state("TODO")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);

        taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getId())
                .name("Task2")
                .description("This is a test task2")
                .assignees(new ArrayList<>())
                .sprintId(null)
                .milestoneId(null)
                .tags(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .state("TODO")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);

        taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getId())
                .name("Task3")
                .description("This is a test task3")
                .assignees(new ArrayList<>())
                .sprintId(null)
                .milestoneId(null)
                .tags(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .state("TODO")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);

        User user = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException("해당 유저를 찾을 수 없습니다.", userId));

        if(user.getWorkspaceId() == null) {
            user.setWorkspaceId(workspaceResponse.id());
        }
    }
}
