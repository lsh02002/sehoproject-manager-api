package com.sehoprojectmanagerapi.service.workspace;

import com.sehoprojectmanagerapi.repository.common.MenuType;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.service.exceptions.AccessDeniedException;
import com.sehoprojectmanagerapi.service.project.ProjectService;
import com.sehoprojectmanagerapi.service.space.SpaceService;
import com.sehoprojectmanagerapi.service.task.TaskService;
import com.sehoprojectmanagerapi.web.dto.project.ProjectRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import com.sehoprojectmanagerapi.web.dto.space.SpaceRequest;
import com.sehoprojectmanagerapi.web.dto.space.SpaceResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskRequest;
import com.sehoprojectmanagerapi.web.mapper.WorkspaceMapper;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRepository;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMember;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMemberRepository;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.workspace.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;
    private final SpaceRepository spaceRepository;

    private final SpaceService spaceService;
    private final ProjectService projectService;
    private final TaskService taskService;

    @Transactional
    public WorkspaceTreeResponse getWorkspaceTree(Long userId, Long workspaceId) {
        // 1) 멤버십 먼저 확인 (권한 노출 방지)
        boolean isMember = workspaceMemberRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);
        if (!isMember) {
            throw new AccessDeniedException("워크스페이스 멤버만 조회할 수 있습니다.", null); // 403에 매핑
        }

        // 2) 워크스페이스 존재 확인 (이 시점에는 멤버만 알 수 있음)
        var workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", workspaceId));

        // 3) 트리 로우 조회 (정렬 보장: space ASC, project ASC 등)
        //    쿼리 자체를 ORDER BY space_position, project_position 로 보장하는 걸 권장
        List<TreeRow> rows = spaceRepository.findTreeRowsByWorkspaceId(workspaceId);

        // 4) 중복 방지: spaceId → (spaceName, projectsSet)
        Map<Long, WorkspaceTreeResponse.SpaceNode> spaceMap = new LinkedHashMap<>();
        // spaceId 별로 이미 추가된 projectId를 추적
        Map<Long, Set<Long>> seenProjectIdsBySpace = new HashMap<>();

        for (TreeRow row : rows) {
            // Space 생성
            spaceMap.computeIfAbsent(row.spaceId(), id ->
                    new WorkspaceTreeResponse.SpaceNode(
                            id,
                            row.spaceName(),
                            MenuType.SPACE,
                            new ArrayList<>()
                    )
            );

            // Project 중복 방지 + null 방어
            if (row.projectId() != null) {
                Set<Long> seen = seenProjectIdsBySpace.computeIfAbsent(row.spaceId(), k -> new HashSet<>());
                if (seen.add(row.projectId())) {
                    spaceMap.get(row.spaceId())
                            .projectNodes()
                            .add(new WorkspaceTreeResponse.ProjectNode(
                                    row.projectId(),
                                    row.projectName(),
                                    MenuType.PROJECT
                            ));
                }
            }
        }

        // 5) 최종 응답: 필요 시 불변 컬렉션으로 래핑
        List<WorkspaceTreeResponse.SpaceNode> spaceNodes = new ArrayList<>(spaceMap.values());
        // spaceNodes.forEach(sn -> sn.setProjectNodes(List.copyOf(sn.getProjectNodes()))); // 가변 → 불변 전환 (게터/세터 유무에 맞게 조정)

        return new WorkspaceTreeResponse(
                workspace.getId(),
                workspace.getName(),
                MenuType.WORKSPACE,
                spaceNodes
        );
    }


    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, WorkspaceRequest request) {
        if (workspaceRepository.existsBySlug(request.slug())) {
            throw new ConflictException("중복된 워크스페이스 슬러그입니다.", request.slug());
        }

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", userId));

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .slug(request.slug())
                .createdBy(creator)
                .build();
        workspace = workspaceRepository.save(workspace);

        // 작성자 = OWNER 로 멤버십 자동 생성
        WorkspaceMember owner = WorkspaceMember.builder()
                .workspace(workspace)
                .user(creator)
                .role(WorkspaceRole.OWNER)
                .joinedAt(OffsetDateTime.now())
                .build();
        workspaceMemberRepository.save(owner);

        WorkspaceResponse response = workspaceMapper.toResponse(workspace);

        createSample(userId, request, response);

        return response;
    }

    @Transactional
    public List<WorkspaceResponse> listWorkspaces(Long userId) {
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByUserId(userId);

        return workspaceMembers
                .stream().map(member->workspaceMapper.toResponse(member.getWorkspace())).toList();
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
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(userId, id)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스 멤버만 수정할 수 있습니다.", null));
        if (!(role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN)) {
            throw new NotAcceptableException("OWNER 또는 ADMIN만 수정할 수 있습니다.", null);
        }

        if (!workspace.getSlug().equals(request.slug()) && workspaceRepository.existsBySlug(request.slug())) {
            throw new ConflictException("중복된 워크스페이스 슬러그입니다.", request.slug());
        }

        workspace.setName(request.name());
        workspace.setSlug(request.slug());

        workspace = workspaceRepository.save(workspace);

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public void deleteWorkspace(Long currentUserId, Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(currentUserId, id)
                .orElseThrow(() -> new BadCredentialsException("워크스페이스 멤버만 삭제할 수 있습니다.", null));
        if (role != WorkspaceRole.OWNER) {
            throw new BadCredentialsException("OWNER만 워크스페이스를 삭제할 수 있습니다.", null);
        }

        workspaceRepository.delete(workspace);
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
                .title("Task1")
                .description("This is a test task1")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getId())
                .title("Task2")
                .description("This is a test task2")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest);

        taskRequest = TaskRequest.builder()
                .projectId(projectResponse.getId())
                .title("Task3")
                .description("This is a test task3")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
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
                .title("Task1")
                .description("This is a test task1")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);

        taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getId())
                .title("Task2")
                .description("This is a test task2")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);

        taskRequest2 = TaskRequest.builder()
                .projectId(projectResponse2.getId())
                .title("Task3")
                .description("This is a test task3")
                .assigneeId(userId)
                .assigneeType("USER")
                .dynamicAssign(false)
                .sprintId(null)
                .milestoneId(null)
                .tagIds(null)
                .dependencyTaskIds(null)
                .priority("MEDIUM")
                .type("TASK")
                .storyPoints(null)
                .dueDate(LocalDate.now())
                .build();

        taskService.createTask(userId, taskRequest2);
    }
}
