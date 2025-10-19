package com.sehoprojectmanagerapi.service.project;

import com.sehoprojectmanagerapi.config.rolefunction.RoleFunc;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.common.CommonStatus;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.repository.space.SpaceRole;
import com.sehoprojectmanagerapi.repository.space.spacemember.SpaceMemberRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.project.ProjectRequest;
import com.sehoprojectmanagerapi.web.dto.project.ProjectResponse;
import com.sehoprojectmanagerapi.web.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import static com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject.MANAGER;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final RoleFunc roleFunc;
    private final SpaceRepository spaceRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public List<ProjectResponse> getAllTeamsByUser(Long userId) {
        return projectMemberRepository.findByUserId(userId)
                .stream().map(projectMember -> projectMapper.toProjectResponse(projectMember.getProject())).toList();
    }

    @Transactional
    public List<ProjectResponse> getAllProjectsByUserAndSpace(Long userId, Long spaceId) {
        spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", null));

        return projectMemberRepository.findByUserId(userId)
                .stream().filter(projectMember -> Objects.equals(projectMember.getProject().getSpace().getId(), spaceId))
                .map(projectMember -> projectMapper.toProjectResponse(projectMember.getProject())).toList();
    }

    @Transactional
    public ProjectResponse getProjectById(Long userId, Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", null));

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

        var role = spaceMemberRepository.findRoleBySpaceIdAndUserId(space.getId(), user.getId())
                .orElseThrow(() -> new NotAcceptableException("해당 프로젝트를 생성할 권한이 없습니다.", null));
        if (role != SpaceRole.ADMIN) {
            throw new NotAcceptableException("스페이스 ADMIN만 프로젝트를 생성할 수 있습니다.", null);
        }

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

        activityLogService.log(ActivityEntityType.PROJECT, ActivityAction.CREATE, savedProject.logTargetId(), savedProject.logMessage(), user, savedProject.logProject(), project, savedProject);

        return projectMapper.toProjectResponse(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest projectRequest) {

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

        activityLogService.log(ActivityEntityType.PROJECT, ActivityAction.UPDATE, savedProject.logTargetId(), savedProject.logMessage(), projectMember.getUser(), savedProject.logProject(), project, savedProject);

        return projectMapper.toProjectResponse(savedProject);
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotAcceptableException("프로젝트 삭제 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("프로젝트 삭제 권한이 없습니다.", userId);
        }

        activityLogService.log(ActivityEntityType.PROJECT, ActivityAction.DELETE, projectMember.getProject().logTargetId(), projectMember.getProject().logMessage(), projectMember.getUser(), projectMember.getProject().logProject(), projectMember.getProject(), null);

        projectMemberRepository.deleteByUserIdAndProjectId(userId, projectId);
        projectRepository.deleteById(projectId);
    }
}
