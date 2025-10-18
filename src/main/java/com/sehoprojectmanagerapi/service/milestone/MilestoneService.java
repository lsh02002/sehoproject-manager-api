package com.sehoprojectmanagerapi.service.milestone;

import com.sehoprojectmanagerapi.config.rolefunction.RoleFunc;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.milestone.MilestoneRepository;
import com.sehoprojectmanagerapi.repository.milestone.MilestoneStatus;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.TaskRepository;
import com.sehoprojectmanagerapi.repository.team.teammember.TeamMemberRepository;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.CustomBadCredentialsException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneRequest;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneResponse;
import com.sehoprojectmanagerapi.web.mapper.MilestoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {
    private final ProjectMemberRepository projectMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final MilestoneMapper milestoneMapper;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final RoleFunc roleFunc;
    private final TaskRepository taskRepository;

    @Transactional
    public List<MilestoneResponse> getAllMilestonesByUserIdAndProjectId(Long userId, Long projectId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotAcceptableException("해당 마일스톤 접근 권한이 없습니다.", null));

        return milestoneRepository.findByProjectId(projectId)
                .stream().map(milestoneMapper::toResponse).toList();
    }

    @Transactional
    public MilestoneResponse getMilestoneById(Long userId, Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new NotFoundException("해당 마일스톤을 찾을 수 없습니다.", milestoneId));

        // 2. 권한 확인
        // (a) 프로젝트 생성자이거나
        // (b) 팀 Owner 이어야 함
        Project project = milestone.getProject();

        projectMemberRepository.findByUserIdAndProjectId(userId, project.getId())
                .orElseThrow(() -> new NotAcceptableException("해당 마일스톤 접근 권한이 없습니다.", milestoneId));

        return milestoneMapper.toResponse(milestone);
    }

    @Transactional
    public MilestoneResponse createMilestone(Long userId, MilestoneRequest request) {
        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", request.projectId()));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("마일스톤 생성 권한이 없습니다.", userId);
        }

        if (request.name().isEmpty()) {
            throw new BadRequestException("해당 제목란이 비어있습니다.", request.name());
        }

        if (request.status().isEmpty()) {
            throw new BadRequestException("해당 상태란이 비어있습니다.", request.status());
        }

        List<Task> tasks = taskRepository.findAllById(request.taskIds() != null ? request.taskIds() : new ArrayList<>());
        for (Task task : tasks) {
            if (!task.getProject().getId().equals(request.projectId())) {
                throw new BadRequestException("모든 태스크는 마일스톤과 같은 프로젝트에 속해야 합니다.", null);
            }
        }

        Milestone milestone = new Milestone();
        milestone.setProject(projectMember.getProject());
        milestone.setName(request.name());
        milestone.setDescription(request.description() != null ? request.description() : "");
        milestone.setStartDate(request.startDate());
        milestone.setDueDate(request.dueDate());
        milestone.setStatus(MilestoneStatus.valueOf(request.status()));
        milestone.setTasks(tasks);

        milestoneRepository.save(milestone);

        return milestoneMapper.toResponse(milestone);
    }

    @Transactional
    public MilestoneResponse updateMilestone(Long userId, Long milestoneId, MilestoneRequest request) {
        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 마일스톤 수정 권한이 없습니다.", request.projectId()));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("해당 마일스톤 수정 권한이 없습니다.", userId);
        }

        if (request.name() == null || request.name().isEmpty()) {
            throw new BadRequestException("해당 제목란이 비어있습니다.", request.name());
        }

        if (request.status() == null || request.status().isEmpty()) {
            throw new BadRequestException("해당 상태란이 비어있습니다.", request.status());
        }

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new NotFoundException("해당 마일스톤을 찾을 수 없습니다.", milestoneId));

        // 날짜 유효성: 요청 값 기준으로 검사
        if (request.startDate() != null && request.dueDate() != null
                && request.dueDate().isBefore(request.startDate())) {
            throw new BadRequestException("마감일은 시작일 이후여야 합니다.", null);
        }

        if(request.taskIds() != null) {
            // ====== 태스크 연결 갱신 ======
            if (request.taskIds().isEmpty()) {
                // 빈 리스트면 전부 해제: owning side도 함께 끊기
                for (Task t : milestone.getTasks()) {
                    t.setMilestone(null);
                }
                milestone.getTasks().clear();
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
                Long projectId = milestone.getProject().getId();
                boolean anyMismatch = tasks.stream()
                        .anyMatch(t -> !t.getProject().getId().equals(projectId));
                if (anyMismatch) {
                    throw new BadRequestException("모든 태스크는 마일스톤과 같은 프로젝트에 속해야 합니다.", null);
                }

                // 이미 다른 마일스톤에 배정된 태스크 거부(재배정 금지 정책)
                List<Long> occupied = tasks.stream()
                        .filter(t -> t.getMilestone() != null && !t.getMilestone().getId().equals(milestone.getId()))
                        .map(Task::getId)
                        .toList();
                if (!occupied.isEmpty()) {
                    throw new BadRequestException("다른 마일스톤에 이미 배정된 태스크가 포함되어 있습니다: " + occupied, null);
                }

                // 교체 (양방향 동기화: owning side 우선 해제 후 설정)
                for (Task t : new ArrayList<>(milestone.getTasks())) {
                    t.setMilestone(null);
                }
                milestone.getTasks().clear();

                for (Task t : tasks) {
                    t.setMilestone(milestone);     // owning side
                    milestone.getTasks().add(t);   // inverse side
                }
            }
        }
        // ====== 기본 필드 갱신 ======
        milestone.setProject(projectMember.getProject());
        milestone.setName(request.name());
        milestone.setDescription(request.description());
        milestone.setStartDate(request.startDate());
        milestone.setDueDate(request.dueDate());
        milestone.setStatus(MilestoneStatus.valueOf(request.status()));

        milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(milestone);
    }

    @Transactional
    public void deleteMilestone(Long userId, Long milestoneId) {
        // 1. 마일스톤 조회
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new NotFoundException("해당 마일스톤을 찾을 수 없습니다.", milestoneId));

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, milestone.getProject().getId())
                .orElseThrow(()->new CustomBadCredentialsException("해당 마일스톤을 삭제할 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("해당 마일스톤을 삭제할 권한이 없습니다.", userId);
        }

        // 3. 삭제 수행
        taskRepository.detachTasksFromMilestone(milestone.getId());
        milestoneRepository.delete(milestone);
    }
}
