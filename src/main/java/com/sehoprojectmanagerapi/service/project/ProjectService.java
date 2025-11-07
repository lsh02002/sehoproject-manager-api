package com.sehoprojectmanagerapi.service.project;

import com.sehoprojectmanagerapi.config.function.RoleFunc;
import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
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
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.tag.TagRepository;
import com.sehoprojectmanagerapi.repository.task.Task;
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
import java.util.*;

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
    private final SnapshotFunc snapshotFunc;
    private final TagRepository tagRepository;

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

        Space space = spaceRepository.findById(projectRequest.getSpaceId())
                .orElseThrow(() -> new NotFoundException("해당 스페이스를 찾을 수 없습니다.", projectRequest.getSpaceId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        var role = spaceMemberRepository.findRoleBySpaceIdAndUserId(space.getId(), user.getId())
                .orElseThrow(() -> new NotAcceptableException("해당 프로젝트를 생성할 권한이 없습니다.", null));
        if (role != SpaceRole.ADMIN) {
            throw new NotAcceptableException("스페이스 ADMIN만 프로젝트를 생성할 수 있습니다.", null);
        }

        if (projectRequest.getName() == null || projectRequest.getName().trim().isEmpty()) {
            throw new BadRequestException("프로젝트명이 비어있습니다.", null);
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

        Object afterproject = snapshotFunc.snapshot(savedProject);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(savedProject);
        projectMember.setUser(user);
        projectMember.setRole(MANAGER);
        projectMember.setJoinedAt(OffsetDateTime.now());

        projectMemberRepository.save(projectMember);

        activityLogService.log(ActivityEntityType.PROJECT, ActivityAction.CREATE, savedProject.logTargetId(), savedProject.logMessage(), user, savedProject.logProject(), null, afterproject);

        return projectMapper.toProjectResponse(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest projectRequest) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다", projectId));

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotFoundException("해당 팀에 본 사용자는 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("프로젝트 수정 권한이 없습니다.", userId);
        }

        // project는 영속 상태여야 함 (@Transactional 내 조회 가정)
        Project project = projectMember.getProject();

        Object beforeproject = snapshotFunc.snapshot(project);

        // -------- 문자열 필드: null이면 유지, ""이면 null로 지우기 --------
        if (projectRequest.getName() != null) {
            String name = projectRequest.getName().trim();
            project.setName(name.isEmpty() ? null : name);
        }
        if (projectRequest.getDescription() != null) {
            String desc = projectRequest.getDescription().trim();
            project.setDescription(desc.isEmpty() ? null : desc);
        }

        // -------- 날짜 필드 --------
        if (projectRequest.getStartDate() != null) {
            project.setStartDate(projectRequest.getStartDate());
        }
        if (projectRequest.getDueDate() != null) {
            project.setDueDate(projectRequest.getDueDate());
        }

        // -------- 작성자 변경 --------
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

        // =====================================================================
        // 태그 동기화: project의 @OneToMany(tags)만 사용 (cascade = ALL, orphanRemoval = true 필요)
        // - ManyToMany(task_tag) FK 에러 방지를 위해 삭제 전 Task와의 연관을 먼저 해제
        // - 생성 로그는 ID가 필요한 관계로 saveAndFlush 이후 기록
        // =====================================================================
        Collection<Tag> tags = project.getTags(); // List/Set 어떤 타입이든 동작
        List<Tag> createdBuffer = new ArrayList<>(); // flush 후 CREATE 로그용 버퍼

        if (projectRequest.getTags() == null) {
            // 요청이 null이면 전체 삭제로 해석
            Iterator<Tag> it = tags.iterator();
            while (it.hasNext()) {
                Tag tag = it.next();

                // 1) Task 연관 해제 (owning side에서 제거)
                if (tag.getTasks() != null) {
                    for (Task task : new ArrayList<>(tag.getTasks())) {
                        task.getTags().remove(tag); // 조인테이블 row 제거
                    }
                    tag.getTasks().clear(); // 역방향 정리(선택)
                }

                // 2) 스냅샷 & 로그(DELETE는 ID가 있으므로 즉시 가능)
                Object beforeTag = snapshotFunc.snapshot(tag);

                // 3) Project에서 제거 → orphanRemoval=true면 DB에서 tag 삭제
                it.remove();

                activityLogService.log(
                        ActivityEntityType.TAG, ActivityAction.DELETE,
                        tag.logTargetId(), tag.logMessage(),
                        projectMember.getUser(), tag.logProject(),
                        beforeTag, null
                );
            }
        } else {
            // 1) 요청 태그 정규화 (비교키: trim().toLowerCase(), 저장은 원문 보존)
            Map<String, String> incomingKeyToOriginal = new LinkedHashMap<>();
            for (String t : projectRequest.getTags()) {
                if (t == null) continue;
                String original = t.trim();
                if (original.isEmpty()) continue;
                String key = original.toLowerCase();
                incomingKeyToOriginal.putIfAbsent(key, original);
            }
            Set<String> incomingKeys = incomingKeyToOriginal.keySet();

            // 2) 기존 태그를 key로 매핑
            Map<String, Tag> existingByKey = new LinkedHashMap<>();
            for (Tag t : tags) {
                String name = Optional.ofNullable(t.getName()).orElse("").trim();
                if (!name.isEmpty()) {
                    existingByKey.put(name.toLowerCase(), t);
                }
            }
            Set<String> existingKeys = existingByKey.keySet();

            // 3) 삭제: 기존에는 있는데 요청에는 없는 것들
            Iterator<Tag> it = tags.iterator();
            while (it.hasNext()) {
                Tag tag = it.next();
                String key = Optional.ofNullable(tag.getName()).orElse("").trim().toLowerCase();
                if (key.isEmpty()) continue;

                if (!incomingKeys.contains(key)) {
                    // Task 연관 해제
                    if (tag.getTasks() != null) {
                        for (Task task : new ArrayList<>(tag.getTasks())) {
                            task.getTags().remove(tag);
                        }
                        tag.getTasks().clear();
                    }

                    Object beforeTag = snapshotFunc.snapshot(tag);
                    it.remove(); // orphanRemoval 로 실제 삭제됨

                    activityLogService.log(
                            ActivityEntityType.TAG, ActivityAction.DELETE,
                            tag.logTargetId(), tag.logMessage(),
                            projectMember.getUser(), tag.logProject(),
                            beforeTag, null
                    );
                }
            }

            // 4) 생성: 요청에는 있고 기존에는 없는 것들
            for (String key : incomingKeys) {
                if (!existingKeys.contains(key)) {
                    String originalName = incomingKeyToOriginal.get(key);

                    Tag created = Tag.builder()
                            .name(originalName)
                            .project(project)
                            .description(null)
                            .build();

                    created = tagRepository.save(created);

                    // 양방향 관계 모두 설정
                    project.getTags().add(created);

                    // 아직 ID 없음 → flush 후 로그
                    createdBuffer.add(created);
                } else {
                    // [선택] 같은 키(대소문자/공백 무시)인데 표기만 다르면 저장 원문으로 덮고 싶을 때:
                    // Tag exist = existingByKey.get(key);
                    // String desired = incomingKeyToOriginal.get(key);
                    // if (!Objects.equals(exist.getName(), desired)) {
                    //     exist.setName(desired);
                    //     // 필요 시 UPDATE 로그 처리
                    // }
                }
            }
        }
        // ===================== 태그 동기화 끝 =====================

        // 또는: projectRepository.save(project); entityManager.flush();

        // 이제 생성(CREATE) 로그 기록 (ID가 존재)
        for (Tag created : createdBuffer) {
            Object afterTag = snapshotFunc.snapshot(created);
            activityLogService.log(
                    ActivityEntityType.TAG, ActivityAction.CREATE,
                    created.logTargetId(), created.logMessage(),
                    projectMember.getUser(), created.logProject(),
                    null, afterTag
            );
        }

        Object afterproject = snapshotFunc.snapshot(project);

        activityLogService.log(
                ActivityEntityType.PROJECT, ActivityAction.UPDATE,
                project.logTargetId(), project.logMessage(),
                projectMember.getUser(), project.logProject(),
                beforeproject, afterproject
        );

        // 생성된 태그의 ID 보장을 위해 즉시 flush
        projectRepository.save(project);

        return projectMapper.toProjectResponse(project);
    }


    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다", projectId));

        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotAcceptableException("프로젝트 삭제 권한이 없습니다.", userId));

        if (!roleFunc.hasAtLeast(projectMember.getRole(), RoleProject.MANAGER)) {
            throw new NotAcceptableException("프로젝트 삭제 권한이 없습니다.", userId);
        }

        Object beforeproject = snapshotFunc.snapshot(projectMember.getProject());

        activityLogService.log(ActivityEntityType.PROJECT, ActivityAction.DELETE, projectMember.getProject().logTargetId(), projectMember.getProject().logMessage(), projectMember.getUser(), projectMember.getProject().logProject(), beforeproject, null);

        projectMemberRepository.deleteByUserIdAndProjectId(userId, projectId);
        projectRepository.deleteById(projectId);
    }
}
