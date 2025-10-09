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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public List<WorkspaceTreeResponse> getWorkspaceTrees(Long userId) {
        // 0) 유저가 속한 워크스페이스 목록을 먼저 전부 조회 (권한 노출 방지)
        //    존재 안 하면 403/404 중 정책에 맞게 처리
        List<Long> workspaceIds = workspaceMemberRepository.findWorkspaceIdsByUserId(userId);
        if (workspaceIds.isEmpty()) {
            // 멤버십이 하나도 없으면 접근 금지 혹은 빈 리스트 반환 중 정책 선택
            throw new AccessDeniedException("소속된 워크스페이스가 없습니다.", null);
            // return List.of();
        }

        // 1) 워크스페이스 엔티티 일괄 조회 (멤버만 이름을 알 수 있음)
        //    Map으로 빠른 참조
        List<Workspace> workspaces = workspaceRepository.findAllById(workspaceIds);
        Map<Long, Workspace> workspaceById = workspaces.stream()
                .collect(Collectors.toMap(Workspace::getId, Function.identity()));

        // 2) 모든 워크스페이스에 대한 트리 로우를 한 번에 조회 (정렬 보장)
        //    ORDER BY 는 쿼리에서 workspace_position, space_position, project_position 등으로 보장
        List<TreeRow> rows = spaceRepository.findTreeRowsByWorkspaceIds(workspaceIds);

        // 3) 워크스페이스별로 그룹핑하며 트리 구성
        //    workspaceId -> (spaceId -> SpaceNode), workspaceId -> (spaceId -> seenProjectIds)
        Map<Long, LinkedHashMap<Long, WorkspaceTreeResponse.SpaceNode>> spaceMapByWorkspace = new LinkedHashMap<>();
        Map<Long, Map<Long, Set<Long>>> seenProjectIdsByWorkspaceAndSpace = new HashMap<>();

        for (TreeRow row : rows) {
            Long wid = row.getWorkspaceId();
            Long sid = row.getSpaceId();

            // 워크스페이스별 Space 맵 꺼내기
            LinkedHashMap<Long, WorkspaceTreeResponse.SpaceNode> spaceMap =
                    spaceMapByWorkspace.computeIfAbsent(wid, k -> new LinkedHashMap<>());

            // Space 노드 생성/중복 방지
            spaceMap.computeIfAbsent(sid, id ->
                    new WorkspaceTreeResponse.SpaceNode(
                            id,
                            row.getSpaceName(),
                            MenuType.SPACE,
                            new ArrayList<>()
                    )
            );

            // 프로젝트 중복 방지 + null 방어
            if (row.getProjectId() != null) {
                Map<Long, Set<Long>> seenBySpace =
                        seenProjectIdsByWorkspaceAndSpace.computeIfAbsent(wid, k -> new HashMap<>());
                Set<Long> seenProjects =
                        seenBySpace.computeIfAbsent(sid, k -> new HashSet<>());

                if (seenProjects.add(row.getProjectId())) {
                    spaceMap.get(sid)
                            .projectNodes()
                            .add(new WorkspaceTreeResponse.ProjectNode(
                                    row.getProjectId(),
                                    row.getProjectName(),
                                    MenuType.PROJECT
                            ));
                }
            }
        }

        // 4) 워크스페이스 순서대로 최종 응답 만들기
        //    workspace 정렬 기준(포지션)이 있다면 쿼리/정렬로 먼저 workspaceIds 자체를 원하는 순서로 받아오면 더 깔끔
        List<WorkspaceTreeResponse> result = new ArrayList<>();
        for (Long wid : workspaceIds) {
            Workspace ws = workspaceById.get(wid);
            if (ws == null) {
                // 방어 코드: 혹시 멤버십은 있는데 워크스페이스가 삭제되었거나 조회 실패한 경우 스킵/예외
                continue;
            }
            List<WorkspaceTreeResponse.SpaceNode> spaceNodes = new ArrayList<>();
            LinkedHashMap<Long, WorkspaceTreeResponse.SpaceNode> spaceMap = spaceMapByWorkspace.get(wid);
            if (spaceMap != null) {
                spaceNodes.addAll(spaceMap.values());
                // 필요하면 불변 래핑
                // spaceNodes = List.copyOf(spaceNodes);
                // spaceNodes.forEach(sn -> sn.setProjectNodes(List.copyOf(sn.getProjectNodes())));
            }

            result.add(new WorkspaceTreeResponse(
                    ws.getId(),
                    ws.getName(),
                    MenuType.WORKSPACE,
                    spaceNodes
            ));
        }

        return result;
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
