package com.sehoprojectmanagerapi.service.sprint;

import com.sehoprojectmanagerapi.config.function.RoleFunc;
import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.sprint.SprintRepository;
import com.sehoprojectmanagerapi.repository.sprint.SprintState;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.TaskRepository;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.CustomBadCredentialsException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintRequest;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintResponse;
import com.sehoprojectmanagerapi.web.mapper.sprint.SprintMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {
    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;
    private final UserRepository userRepository;
    private final RoleFunc roleFunc;
    private final TaskRepository taskRepository;
    private final ActivityLogService activityLogService;
    private final SnapshotFunc snapshotFunc;
    private final ProjectRepository projectRepository;

    /**
     * 사용자 기준 가시한 스프린트 전체 조회
     */
    @Transactional
    public List<SprintResponse> getAllSprintsByUserIdAndProjectId(Long userId, Long projectId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", projectId));

        projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotAcceptableException("해당 스프린트에 접근 권한이 없습니다.", null));

        return sprintRepository.findByProjectId(projectId)
                .stream().map(sprintMapper::toResponse).toList();
    }

    @Transactional
    public SprintResponse getSprintById(Long userId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("해당 스프린트를 찾을 수 없습니다.", sprintId));

        Project project = sprint.getProject();

        projectMemberRepository.findByUserIdAndProjectId(userId, project.getId())
                .orElseThrow(() -> new NotAcceptableException("해당 스프린트에 접근 권한이 없습니다.", sprintId));

        return sprintMapper.toResponse(sprint);
    }

    /**
     * 스프린트 생성
     */
    @Transactional
    public SprintResponse createSprint(Long userId, SprintRequest request) {
        ProjectMember projectMember = projectMemberRepository
                .findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트 접근 권한이 없습니다.", request.projectId()));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("해당 스프린트 생성 권한이 없습니다.", userId);
        }

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new BadRequestException("스프린트명이 비어있습니다.", request.name());
        }
        if (request.state() == null || request.state().trim().isEmpty()) {
            throw new BadRequestException("스프린트 상태가 비어있습니다.", request.state());
        }

        List<Task> tasks = taskRepository.findAllById(request.taskIds() != null ? request.taskIds() : new ArrayList<>());
        for (Task task : tasks) {
            if (!task.getProject().getId().equals(request.projectId())) {
                throw new BadRequestException("모든 태스크는 스프린트와 같은 프로젝트에 속해야 합니다.", null);
            }
        }

        Sprint sprint = new Sprint();
        sprint.setProject(projectMember.getProject());
        sprint.setName(request.name().trim());            // 선택
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setState(SprintState.valueOf(request.state()));
        sprint.setTasks(tasks);

        Sprint savedsprint = sprintRepository.save(sprint);

        Object aftersprint = sprintMapper.toResponse(savedsprint);

        activityLogService.log(ActivityEntityType.SPRINT, ActivityAction.CREATE, savedsprint.getId(), savedsprint.logMessage(), projectMember.getUser(), null, aftersprint);

        return sprintMapper.toResponse(sprint);
    }

    /**
     * 스프린트 수정
     */
    @Transactional
    public SprintResponse updateSprint(Long userId, Long sprintId, SprintRequest request) {
        projectRepository.findById(request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", request.projectId()));

        ProjectMember projectMember = projectMemberRepository
                .findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트 접근 권한이 없습니다.", request.projectId()));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("해당 스프린트 수정 권한이 없습니다.", userId);
        }

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new BadRequestException("스프린트명이 비어있습니다.", request.name());
        }
        if (request.state() == null || request.state().trim().isEmpty()) {
            throw new BadRequestException("스프린트 상태가 비어있습니다.", request.state());
        }

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("해당 스프린트를 찾을 수 없습니다.", sprintId));

        Object beforesprint = snapshotFunc.snapshot(sprint);

        if (request.taskIds() != null) {
            // ====== 태스크 연결 갱신 ======
            if (request.taskIds().isEmpty()) {
                // 빈 리스트면 전부 해제: owning side도 함께 끊기
                for (Task t : sprint.getTasks()) {
                    t.setSprint(null);
                }
                sprint.getTasks().clear();
            } else {
                // 목록으로 교체
                List<Task> tasks = taskRepository.findAllByIdInAndProjectId(
                        request.taskIds(), projectMember.getProject().getId()
                );

                // 누락된 ID 체크 (요청 개수 != 조회 개수)
                if (tasks.size() != request.taskIds().size()) {
                    Set<Long> found = tasks.stream().map(Task::getId).collect(Collectors.toSet());
                    List<Long> missing = request.taskIds().stream()
                            .filter(id -> !found.contains(id)).toList();
                    throw new NotFoundException("해당 태스크를 찾을 수 없습니다: " + missing, null);
                }

                // 프로젝트 일치 검증 (방어적 중복검사)
                Long projectId = sprint.getProject().getId();
                boolean anyMismatch = tasks.stream()
                        .anyMatch(t -> !t.getProject().getId().equals(projectId));
                if (anyMismatch) {
                    throw new BadRequestException("모든 태스크는 스프린트와 같은 프로젝트에 속해야 합니다.", null);
                }

                // 이미 다른 스프린트에 배정된 태스크 거부(재배정 금지 정책)
                List<Long> occupied = tasks.stream()
                        .filter(t -> t.getSprint() != null && !t.getSprint().getId().equals(sprint.getId()))
                        .map(Task::getId)
                        .toList();
                if (!occupied.isEmpty()) {
                    throw new BadRequestException("다른 스프린트에 이미 배정된 태스크가 포함되어 있습니다: " + occupied, null);
                }

                // 교체 (양방향 동기화: owning side 우선 해제 후 설정)
                for (Task t : new ArrayList<>(sprint.getTasks())) {
                    t.setSprint(null);
                }
                sprint.getTasks().clear();

                for (Task t : tasks) {
                    t.setSprint(sprint);     // owning side
                    sprint.getTasks().add(t);   // inverse side
                }
            }
        }

        // 같은 프로젝트 기준으로만 업데이트(프로젝트 이동 허용 정책이면 아래 라인 유지)
        sprint.setProject(projectMember.getProject());
        sprint.setName(request.name().trim());           // 선택
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setState(SprintState.valueOf(request.state()));

        Object aftersprint = snapshotFunc.snapshot(sprint);

        activityLogService.log(ActivityEntityType.SPRINT, ActivityAction.UPDATE, sprint.getId(), sprint.logMessage(), projectMember.getUser(), beforesprint, aftersprint);

        sprintRepository.save(sprint);

        return sprintMapper.toResponse(sprint);
    }

    /**
     * 스프린트 삭제
     */
    @Transactional
    public void deleteSprint(Long userId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("해당 스프린트를 찾을 수 없습니다.", sprintId));

        Object beforesprint = snapshotFunc.snapshot(sprint);

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, sprint.getProject().getId())
                .orElseThrow(() -> new CustomBadCredentialsException("해당 스프린트를 삭제할 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("해당 스프린트 삭제할 권한이 없습니다.", userId);
        }

        activityLogService.log(ActivityEntityType.SPRINT, ActivityAction.DELETE, sprint.getId(), sprint.logMessage(), projectMember.getUser(), beforesprint, null);

        taskRepository.detachTasksFromSprint(sprint.getId());
        sprintRepository.delete(sprint);
    }
}
