package com.sehoprojectmanagerapi.service.sprint;

import com.sehoprojectmanagerapi.config.mapper.SprintMapper;
import com.sehoprojectmanagerapi.config.rolefunction.RoleFunc;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.sprint.Sprint;
import com.sehoprojectmanagerapi.repository.sprint.SprintRepository;
import com.sehoprojectmanagerapi.repository.sprint.SprintState;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.CustomBadCredentialsException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintRequest;
import com.sehoprojectmanagerapi.web.dto.sprint.SprintResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintService {
    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;
    private final UserRepository userRepository;
    private final RoleFunc roleFunc;

    /** 사용자 기준 가시한 스프린트 전체 조회 */
    @Transactional(readOnly = true)
    public List<SprintResponse> getAllSprintsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        return sprintRepository.findAllVisibleForUser(userId)
                .stream().map(sprintMapper::toResponse).toList();
    }

    /** 스프린트 생성 */
    @Transactional
    public SprintResponse createSprint(Long userId, SprintRequest request) {
        ProjectMember projectMember = projectMemberRepository
                .findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", request.projectId()));

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new BadRequestException("스프린트명이 비어있습니다.", request.name());
        }
        if (request.status() == null || request.status().trim().isEmpty()) {
            throw new BadRequestException("스프린트 상태가 비어있습니다.", request.status());
        }

        Sprint sprint = new Sprint();
        sprint.setProject(projectMember.getProject());
        sprint.setName(request.name().trim());            // 선택
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setState(SprintState.valueOf(request.status()));

        sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    /** 스프린트 수정 */
    @Transactional
    public SprintResponse updateSprint(Long userId, Long sprintId, SprintRequest request) {
        ProjectMember projectMember = projectMemberRepository
                .findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", request.projectId()));

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new BadRequestException("스프린트명이 비어있습니다.", request.name());
        }
        if (request.status() == null || request.status().trim().isEmpty()) {
            throw new BadRequestException("스프린트 상태가 비어있습니다.", request.status());
        }

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("해당 스프린트를 찾을 수 없습니다.", sprintId));

        // 같은 프로젝트 기준으로만 업데이트(프로젝트 이동 허용 정책이면 아래 라인 유지)
        sprint.setProject(projectMember.getProject());
        sprint.setName(request.name().trim());           // 선택
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setState(SprintState.valueOf(request.status()));

        sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    /** 스프린트 삭제 */
    @Transactional
    public void deleteSprint(Long userId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new NotFoundException("해당 스프린트를 찾을 수 없습니다.", sprintId));

        Project project = sprint.getProject();
        if (project == null) {
            throw new NotFoundException("스프린트에 연결된 프로젝트가 없습니다.", null);
        }

        // (a) 프로젝트 생성자, 또는 (b) 프로젝트 MANAGER 이상만 삭제 허용
        boolean isProjectCreator = project.getCreatedBy() != null
                && project.getCreatedBy().getId().equals(userId);

        boolean isProjectManagerUp = projectMemberRepository
                .findByUserIdAndProjectId(userId, project.getId())
                .map(pm -> roleFunc.hasAtLeast(pm.getRole(), RoleProject.MANAGER))
                .orElse(false);

        if (!(isProjectCreator || isProjectManagerUp)) {
            throw new CustomBadCredentialsException("해당 스프린트를 삭제할 권한이 없습니다.", userId);
        }

        sprintRepository.delete(sprint);
    }
}
