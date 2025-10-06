package com.sehoprojectmanagerapi.service.task;

import com.sehoprojectmanagerapi.config.keygenerator.TaskKeyGenerator;
import com.sehoprojectmanagerapi.config.mapper.TaskMapper;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.milestone.MilestoneRepository;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.sprint.SprintRepository;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.tag.TagRepository;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.TaskPriority;
import com.sehoprojectmanagerapi.repository.task.TaskRepository;
import com.sehoprojectmanagerapi.repository.task.TaskType;
import com.sehoprojectmanagerapi.repository.task.taskassignee.*;
import com.sehoprojectmanagerapi.repository.task.taskdependency.TaskDependency;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.team.TeamRepository;
import com.sehoprojectmanagerapi.repository.team.teammember.TeamMemberRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.task.TaskRequest;
import com.sehoprojectmanagerapi.web.dto.task.TaskResponse;
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
    public TaskResponse createTask(Long userId, TaskRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", userId));

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new NotFoundException("프로젝트를 찾을 수 없습니다.", request.projectId()));

        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);
        if (!isMember) {
            throw new NotAcceptableException("해당 프로젝트에 대한 권한이 없습니다.", userId);
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
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            tags = tagRepository.findAllById(request.tagIds());
            if (tags.size() != request.tagIds().size()) {
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
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(TaskPriority.from(request.priority())); // 문자열 -> Enum 변환 유틸
        task.setType(TaskType.from(request.type()));             // 문자열 -> Enum 변환 유틸
        task.setStoryPoints(request.storyPoints());
        task.setDueDate(request.dueDate());
        task.setSprint(sprint);
        task.setMilestone(milestone);
        task.setCreatedAt(OffsetDateTime.now().toLocalDateTime());

        // 7) 먼저 저장해서 PK 확보
        task = taskRepository.save(task);

        // 다형 담당자 처리 (없으면 스킵)
        TaskAssignee assigneeSource = null;
        if (request.assigneeId() != null && request.assigneeType() != null) {
            AssigneeType type;
            try {
                type = AssigneeType.valueOf(request.assigneeType());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("유효하지 않은 assigneeType입니다.", request.assigneeType());
            }

            switch (type) {
                case USER -> {
                    User assignee = userRepository.findById(request.assigneeId())
                            .orElseThrow(() -> new NotFoundException("담당자 사용자를 찾을 수 없습니다.", request.assigneeId()));

                    // 정책: 담당자는 프로젝트 멤버여야 함
                    if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), assignee.getId())) {
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
                    Team team = teamRepository.findById(request.assigneeId())
                            .orElseThrow(() -> new NotFoundException("담당자 팀을 찾을 수 없습니다.", request.assigneeId()));

                    boolean dynamic = Boolean.TRUE.equals(request.dynamicAssign()); // 팀 변동 자동 반영 여부
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
                        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), u.getId()) &&
                                !taskAssigneeUserRepository.existsByTaskIdAndUserId(task.getId(), u.getId())) {
                            taskAssigneeUserRepository.save(new TaskAssigneeUser(task, u, assigneeSource));
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
        task = taskRepository.save(task);

        // 12) 응답
        return taskMapper.toTaskResponse(task);
    }
}
