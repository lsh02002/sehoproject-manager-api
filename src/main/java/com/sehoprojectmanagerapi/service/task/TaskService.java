package com.sehoprojectmanagerapi.service.task;

import com.sehoprojectmanagerapi.config.function.RoleFunc;
import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
import com.sehoprojectmanagerapi.config.keygenerator.TaskKeyGenerator;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.milestone.MilestoneRepository;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.sprint.SprintRepository;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.tag.TagRepository;
import com.sehoprojectmanagerapi.repository.task.*;
import com.sehoprojectmanagerapi.repository.task.taskassignee.*;
import com.sehoprojectmanagerapi.repository.task.taskdependency.TaskDependency;
import com.sehoprojectmanagerapi.repository.task.taskdependency.TaskDependencyRepository;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.team.TeamRepository;
import com.sehoprojectmanagerapi.repository.team.teammember.TeamMemberRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeRequest;
import com.sehoprojectmanagerapi.web.dto.task.TaskRequest;
import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
import com.sehoprojectmanagerapi.web.dto.task.TaskUpdateRequest;
import com.sehoprojectmanagerapi.web.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskAssigneeUserRepository taskAssigneeUserRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SprintRepository sprintRepository;
    private final MilestoneRepository milestoneRepository;
    private final TagRepository tagRepository;
    private final TaskKeyGenerator taskKeyGenerator; // 예: "PROJ-123" 생성
    private final TaskMapper taskMapper;             // Entity -> Response
    private final RoleFunc roleFunc;
    private final ActivityLogService activityLogService;
    private final SnapshotFunc snapshotFunc;

    @Transactional
    public List<TaskResponse> getAllTasksByUser(Long userId) {
        List<ProjectMember> projectMember = projectMemberRepository.findByUserId(userId);
        List<TaskResponse> taskAllResponses = new ArrayList<>();

        for (ProjectMember p : projectMember) {
            List<TaskResponse> taskResponses = taskRepository.findByProject(p.getProject())
                    .stream().map(taskMapper::toTaskResponse).toList();

            taskAllResponses.addAll(taskResponses);
        }

        return taskAllResponses;
    }

    @Transactional
    public List<TaskResponse> getAllTasksByUserAndProject(Long userId, Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", null));
        // 멤버십 확인: 권한 예외 사용(403)
        projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotAcceptableException("해당 프로젝트에 접근 권한이 없습니다.", null));

        // 정렬 보장: 리포지토리 쿼리에서 ORDER BY 권장 (createdAt or custom position)
        var tasks = taskRepository.findAllByProjectIdOrderByCreatedAtAsc(projectId);

        return tasks.stream()
                .map(taskMapper::toTaskResponse)
                .toList();
    }

    @Transactional
    public TaskResponse getTaskById(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("해당 태스크를 찾을 수 없습니다.", null));

        projectMemberRepository.findByUserIdAndProjectId(userId, task.getProject().getId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트 접근 권한이 없습니다.", null));

        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    public List<TaskResponse> getTasksByAssigneeId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자가 없습니다.", userId));

        return taskRepository.findTasksVisibleToUser(userId)
                .stream().map(taskMapper::toTaskResponse).toList();
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", userId));

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new NotFoundException("프로젝트를 찾을 수 없습니다.", request.projectId()));

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, project.getId())
                .orElseThrow(() -> new NotAcceptableException("해당 프로젝트에 대한 접근 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.CONTRIBUTOR)) {
            throw new NotAcceptableException("해당 태스크 생성 권한이 없습니다.", userId);
        }

        // 3) Sprint/Milestone 검증 (same project)
        Sprint sprint = null;
        if (request.sprintId() != null) {
            sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new NotFoundException("스프린트를 찾을 수 없습니다.", request.sprintId()));
            if (!Objects.equals(sprint.getProject().getId(), project.getId())) {
                throw new ConflictException("스프린트가 프로젝트에 속해 있지 않습니다.", request.sprintId());
            }
        }

        Milestone milestone = null;
        if (request.milestoneId() != null) {
            milestone = milestoneRepository.findById(request.milestoneId())
                    .orElseThrow(() -> new NotFoundException("마일스톤을 찾을 수 없습니다.", request.milestoneId()));
            if (!Objects.equals(milestone.getProject().getId(), project.getId())) {
                throw new ConflictException("마일스톤이 프로젝트에 속해 있지 않습니다.", request.milestoneId());
            }
        }

        // 4) 태그 로드 (same project 정책이라면 검증)
        List<Tag> tags = List.of();
        if (request.tags() != null && !request.tags().isEmpty()) {
            tags = tagRepository.findAllById(request.tags());
            if (tags.size() != request.tags().size()) {
                throw new NotFoundException("하나 이상의 태그를 찾을 수 없습니다.", null);
            }
            // 프로젝트 범위 태그 정책이면 아래 검증 추가
            for (Tag t : tags) {
                if (!Objects.equals(t.getProject().getId(), project.getId())) {
                    throw new ConflictException("다른 프로젝트의 태그가 포함되어 있습니다.", t.getId());
                }
            }
        }

        // 5) 선행 작업(Dependency) 로드 및 검증
        List<Task> dependencies = List.of();
        if (request.dependencyTaskIds() != null && !request.dependencyTaskIds().isEmpty()) {
            dependencies = taskRepository.findAllById(request.dependencyTaskIds());
            if (dependencies.size() != request.dependencyTaskIds().size()) {
                throw new NotFoundException("하나 이상의 선행 작업을 찾을 수 없습니다.", null);
            }
            for (Task dep : dependencies) {
                if (!Objects.equals(dep.getProject().getId(), project.getId())) {
                    throw new ConflictException("다른 프로젝트의 작업이 의존성에 포함되어 있습니다.", dep.getId());
                }
                if (dep.getId() == null) continue;
                // 새 작업이므로 자기참조는 애초에 불가하지만 방어적으로 체크
                if (request.dependencyTaskIds().contains(dep.getId()) && Objects.equals(dep.getId(), null)) {
                    throw new BadRequestException("자기 자신을 의존성으로 설정할 수 없습니다.", null);
                }
            }
            // (선택) 간단 순환 방지: 서로가 서로를 의존하는 즉시 순환만 차단
            // 새 작업은 아직 그래프에 없으므로 깊은 순환 검사는 생략
        }

        // 6) Task 엔티티 생성
        Task task = new Task();
        task.setCreatedBy(creator);
        task.setProject(project);
        task.setName(request.name());
        task.setDescription(request.description());
        task.setPriority(TaskPriority.from(request.priority())); // 문자열 -> Enum 변환 유틸
        task.setType(TaskType.from(request.type()));             // 문자열 -> Enum 변환 유틸
        task.setState(TaskState.from(request.state()));
        task.setStoryPoints(request.storyPoints());
        task.setDueDate(request.dueDate());
        task.setSprint(sprint);
        task.setMilestone(milestone);
        task.setCreatedAt(OffsetDateTime.now().toLocalDateTime());

        // 7) 먼저 저장해서 PK 확보
        task = taskRepository.save(task);

        // 다형 담당자 처리 (없으면 스킵)
        if (request.assignees() != null) {
            TaskAssignee assigneeSource = null;

            for (AssigneeRequest assigneeRequest : request.assignees()) {
                AssigneeType type;
                try {
                    type = AssigneeType.valueOf(assigneeRequest.getType());
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("유효하지 않은 assigneeType입니다.", assigneeRequest.getType());
                }

                switch (type) {
                    case USER -> {
                        User assignee = userRepository.findById(assigneeRequest.getAssigneeId())
                                .orElseThrow(() -> new NotFoundException("담당자 사용자를 찾을 수 없습니다.", assigneeRequest.getAssigneeId()));

                        // 정책: 담당자는 프로젝트 멤버여야 함
                        if (!projectMemberRepository.existsByUserIdAndProjectId(assignee.getId(), project.getId())) {
                            throw new ConflictException("담당자는 프로젝트 멤버여야 합니다.", assignee.getId());
                        }

                        assigneeSource = taskAssigneeRepository.save(
                                TaskAssignee.forUser(task, assignee, creator, OffsetDateTime.now())
                        );

                        // (선택) 확장 테이블 쓰는 경우
                        if (!taskAssigneeUserRepository.existsByTaskIdAndUserId(task.getId(), assignee.getId())) {
                            taskAssigneeUserRepository.save(new TaskAssigneeUser(task, assignee, assigneeSource));
                        }
                    }
                    case TEAM -> {
                        Team team = teamRepository.findById(assigneeRequest.getAssigneeId())
                                .orElseThrow(() -> new NotFoundException("담당자 팀을 찾을 수 없습니다.", assigneeRequest.getDynamicAssign()));

                        boolean dynamic = Boolean.TRUE.equals(assigneeRequest.getDynamicAssign()); // 팀 변동 자동 반영 여부
                        // 정책: 팀 구성원 모두가 해당 프로젝트 멤버여야 함(엄격)
                        boolean allMembersInProject =
                                teamMemberRepository.countActiveByTeamIdNotInProject(team.getId(), project.getId()) == 0;
                        if (!allMembersInProject) {
                            // 완화 정책을 원하면 여기서 교집합만 확장하도록 분기 가능
                            throw new ConflictException("팀 구성원 전원이 프로젝트 멤버여야 합니다.", team.getId());
                        }

                        assigneeSource = taskAssigneeRepository.save(
                                TaskAssignee.forTeam(task, team, creator, dynamic, OffsetDateTime.now())
                        );

                        // (선택) 확장: 팀 멤버 전원 사용자 단위로 upsert
                        List<User> members = teamMemberRepository.findActiveUsersByTeamId(team.getId());
                        for (User u : members) {
                            if (projectMemberRepository.existsByUserIdAndProjectId(u.getId(), project.getId()) &&
                                    !taskAssigneeUserRepository.existsByTaskIdAndUserId(task.getId(), u.getId())) {
                                taskAssigneeUserRepository.save(new TaskAssigneeUser(task, u, assigneeSource));
                            }
                        }
                    }
                }
            }
        }

        // 8) 키 생성(프로젝트 prefix + 시퀀스 등) 및 갱신
        String key = taskKeyGenerator.generate(project, task.getId()); // 예: "PROJ-123"
        task.getProject().setKey(key);

        // 9) 태그 매핑
        if (!tags.isEmpty()) {
            task.getTags().addAll(tags);
        }

        // 10) 의존성 매핑 (TaskDependency: dependent -> prerequisite)
        if (!dependencies.isEmpty()) {
            Task finalTask = task;
            List<TaskDependency> deps = dependencies.stream().map(prereq -> {
                TaskDependency d = new TaskDependency();
                d.setTask(finalTask);
                d.setDependsOn(prereq);
                return d;
            }).collect(Collectors.toList());
            task.setDependencies(deps);
        }

        // 11) 최종 저장
        Task savedtask = taskRepository.save(task);

        Object aftertask = snapshotFunc.snapshot(savedtask);

        activityLogService.log(ActivityEntityType.TASK, ActivityAction.CREATE, savedtask.logTargetId(), savedtask.logMessage(), projectMember.getUser(), savedtask.logProject(), null, aftertask);

        // 12) 응답
        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        // 0) 공통 로드 & 권한
        User updater = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", userId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("작업(Task)을 찾을 수 없습니다.", taskId));

        Object beforetask = snapshotFunc.snapshot(task);

        Project project = task.getProject();

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, project.getId())
                .orElseThrow(() -> new NotAcceptableException("해당 프로젝트에 대한 접근 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.CONTRIBUTOR)) {
            throw new NotAcceptableException("해당 태스크 업데이트할 권한이 없습니다.", userId);
        }

        // 1) 스프린트/마일스톤 변경 (null = 변경 없음, Optional.empty 의미의 특별 플래그가 없다면: 빈 값 명시 지우기용 필드 권장)
        if (request.sprintId() != null) {
            if (request.sprintId() <= 0) {
                task.setSprint(null); // 0 이하를 '해제' 시그널로 가정
            } else {
                Sprint sprint = sprintRepository.findById(request.sprintId())
                        .orElseThrow(() -> new NotFoundException("스프린트를 찾을 수 없습니다.", request.sprintId()));
                if (!Objects.equals(sprint.getProject().getId(), project.getId())) {
                    throw new ConflictException("스프린트가 프로젝트에 속해 있지 않습니다.", request.sprintId());
                }
                task.setSprint(sprint);
            }
        }

        if (request.milestoneId() != null) {
            if (request.milestoneId() <= 0) {
                task.setMilestone(null);
            } else {
                Milestone milestone = milestoneRepository.findById(request.milestoneId())
                        .orElseThrow(() -> new NotFoundException("마일스톤을 찾을 수 없습니다.", request.milestoneId()));
                if (!Objects.equals(milestone.getProject().getId(), project.getId())) {
                    throw new ConflictException("마일스톤이 프로젝트에 속해 있지 않습니다.", request.milestoneId());
                }
                task.setMilestone(milestone);
            }
        }

        // 2) 기본 필드들 (null => 변경 없음)
        if (request.name() != null) task.setName(request.name());
        if (request.description() != null) task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(TaskPriority.from(request.priority()));
        if (request.type() != null) task.setType(TaskType.from(request.type()));
        if (request.state() != null) task.setState(TaskState.from(request.state()));
        if (request.storyPoints() != null) task.setStoryPoints(request.storyPoints());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        task.setProject(project);

        // 3) 태그 교체 로직
        // - null : 변경 없음
        // - 빈 리스트 : 모두 제거
        // - 그 외 : 동일 프로젝트 소속 검증 후 교체

        if (request.tags().isEmpty()) {
            task.getTags().clear();
        } else {
            List<Tag> tags = tagRepository.findAllById(request.tags());
//            if (tags.size() != request.tags().size()) {
//                throw new NotFoundException("하나 이상의 태그를 찾을 수 없습니다.", null);
//            }
            for (Tag t : tags) {
                if (!Objects.equals(t.getProject().getId(), project.getId())) {
                    throw new ConflictException("다른 프로젝트의 태그가 포함되어 있습니다.", t.getId());
                }
            }

            task.getTags().clear();
            task.getTags().addAll(tags);
        }

        // 4) 의존성 교체 로직 (TaskDependency: dependent -> prerequisite)
        if (request.dependencyTaskIds().isEmpty()) {
            // 모두 제거
            taskDependencyRepository.deleteByTaskId(task.getId());
            task.setDependencies(List.of());
        } else {
            List<Task> deps = taskRepository.findAllById(request.dependencyTaskIds());
            if (deps.size() != request.dependencyTaskIds().size()) {
                throw new NotFoundException("하나 이상의 선행 작업을 찾을 수 없습니다.", null);
            }
            for (Task dep : deps) {
                if (!Objects.equals(dep.getProject().getId(), project.getId())) {
                    throw new ConflictException("다른 프로젝트의 작업이 의존성에 포함되어 있습니다.", dep.getId());
                }
                if (Objects.equals(dep.getId(), task.getId())) {
                    throw new BadRequestException("자기 자신을 의존성으로 설정할 수 없습니다.", task.getId());
                }
            }
            // 간단한 상호(즉시) 순환 방지: A->B 요청인데 B가 이미 A를 선행으로 갖는 케이스 차단
            if (taskDependencyRepository.existsMutualDependency(task.getId(), request.dependencyTaskIds())) {
                throw new ConflictException("상호 의존 순환이 감지되었습니다.", task.getId());
            }

            // 기존 제거 후 새로 세팅 (간단/명확)
            taskDependencyRepository.deleteByTaskId(task.getId());
            Task finalTask = task;
            List<TaskDependency> newDeps = deps.stream().map(prereq -> {
                TaskDependency d = new TaskDependency();
                d.setTask(finalTask);
                d.setDependsOn(prereq);
                return d;
            }).collect(Collectors.toList());
            task.setDependencies(newDeps);
        }


        // 5) 담당자(다형) 업데이트
        // 규칙:
        //  - assigneeType, assigneeId 둘 다 null이면 변경 없음
        //  - assigneeType != null && assigneeId == null 이면 '할당 해제'
        //  - 둘 다 있으면 타입 검증 + 동일 프로젝트 정책 검증 후 재할당
        taskAssigneeUserRepository.deleteByTaskId(task.getId());
        taskAssigneeRepository.deleteByTaskId(task.getId());

        if (request.assignees().isEmpty()) {
            task.getAssignees().clear();
        }

        for (AssigneeRequest assigneeRequest : request.assignees()) {
            if (assigneeRequest.getType() != null) {
                AssigneeType type;
                try {
                    type = AssigneeType.valueOf(assigneeRequest.getType());
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("유효하지 않은 assigneeType입니다.", assigneeRequest.getType());
                }

                TaskAssignee assigneeSource;
                switch (type) {
                    case USER -> {
                        User assignee = userRepository.findById(assigneeRequest.getAssigneeId())
                                .orElseThrow(() -> new NotFoundException("담당자 사용자를 찾을 수 없습니다.", assigneeRequest.getAssigneeId()));
                        if (!projectMemberRepository.existsByUserIdAndProjectId(assignee.getId(), project.getId())) {
                            throw new ConflictException("담당자는 프로젝트 멤버여야 합니다.", assignee.getId());
                        }
                        assigneeSource = taskAssigneeRepository.save(
                                TaskAssignee.forUser(task, assignee, updater, OffsetDateTime.now())
                        );
                        if (!taskAssigneeUserRepository.existsByTaskIdAndUserId(task.getId(), assignee.getId())) {
                            taskAssigneeUserRepository.save(new TaskAssigneeUser(task, assignee, assigneeSource));
                        }
                    }
                    case TEAM -> {
                        Team team = teamRepository.findById(assigneeRequest.getAssigneeId())
                                .orElseThrow(() -> new NotFoundException("담당자 팀을 찾을 수 없습니다.", assigneeRequest.getAssigneeId()));

                        boolean dynamic = Boolean.TRUE.equals(assigneeRequest.getDynamicAssign());
                        boolean allMembersInProject =
                                teamMemberRepository.countActiveByTeamIdNotInProject(team.getId(), project.getId()) == 0;
                        if (!allMembersInProject) {
                            throw new ConflictException("팀 구성원 전원이 프로젝트 멤버여야 합니다.", team.getId());
                        }

                        assigneeSource = taskAssigneeRepository.save(
                                TaskAssignee.forTeam(task, team, updater, dynamic, OffsetDateTime.now())
                        );

                        List<User> members = teamMemberRepository.findActiveUsersByTeamId(team.getId());
                        for (User u : members) {
                            if (projectMemberRepository.existsByUserIdAndProjectId(u.getId(), project.getId()) &&
                                    !taskAssigneeUserRepository.existsByTaskIdAndUserId(task.getId(), u.getId())) {
                                taskAssigneeUserRepository.save(new TaskAssigneeUser(task, u, assigneeSource));
                            }
                        }
                    }
                }
            } else if (Boolean.TRUE.equals(assigneeRequest.getDynamicAssign())) {
                // 타입 변경은 없고, 팀 담당 상태에서 dynamic 플래그만 토글하고 싶은 경우 지원
                taskAssigneeRepository.updateDynamicFlagIfTeam(task.getId(), true);
            } else if (Boolean.FALSE.equals(assigneeRequest.getDynamicAssign())) {
                taskAssigneeRepository.updateDynamicFlagIfTeam(task.getId(), false);
            }
        }
        // 6) (선택) 키 재계산은 일반적으로 불필요. 필요 시 주석 해제
        // String key = taskKeyGenerator.generate(project, task.getId());
        // task.setKey(key);

        // 7) 감사 필드 업데이트(있다면)
//        task.setUpdatedBy(updater);
        task.setUpdatedAt(OffsetDateTime.now().toLocalDateTime());

        // 8) 저장 & 응답
        Task savedtask = taskRepository.save(task);

        Object aftertask = snapshotFunc.snapshot(savedtask);

        activityLogService.log(ActivityEntityType.TASK, ActivityAction.UPDATE, savedtask.logTargetId(), savedtask.logMessage(), projectMember.getUser(), savedtask.logProject(), beforetask, aftertask);

        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long projectId, Long taskId) {
        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotAcceptableException("해당 프로젝트에 접근 권한이 없습니다.", null));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.CONTRIBUTOR)) {
            throw new NotAcceptableException("해당 태스크 삭제할 권한이 없습니다.", userId);
        }

        Task task = taskRepository.findByProjectIdAndId(projectMember.getProject().getId(), taskId)
                .orElseThrow(() -> new NotFoundException("해당 태스크를 찾을 수 없습니다.", null));

        Object beforetask = snapshotFunc.snapshot(task);

        activityLogService.log(ActivityEntityType.TASK, ActivityAction.DELETE, task.logTargetId(), task.logMessage(), projectMember.getUser(), task.logProject(), beforetask, null);

        try {
            taskRepository.delete(task);
        } catch (Exception e) {
            throw new ConflictException("해당 태스크 삭제를 실패했습니다.", taskId);
        }
    }
}
