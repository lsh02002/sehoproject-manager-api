package com.sehoprojectmanagerapi.service.tag;

import com.sehoprojectmanagerapi.config.rolefunction.RoleFunc;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.repository.tag.TagRepository;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.tag.TagRequest;
import com.sehoprojectmanagerapi.web.dto.tag.TagResponse;
import com.sehoprojectmanagerapi.web.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;
    private final RoleFunc roleFunc;
    private final ActivityLogService activityLogService;

    /* 목록 조회: 프로젝트 멤버면 누구나 열람 가능 */
    @Transactional
    public List<TagResponse> getTagsByProject(Long userId, Long projectId) {
        // 사용자/프로젝트 존재 체크
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", projectId));

        // 프로젝트 멤버 여부
        projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotAcceptableException("프로젝트 멤버만 조회할 수 있습니다.", userId));

        return tagRepository.findAllByProjectId(projectId)
                .stream().map(tagMapper::toResponse).toList();
    }

    @Transactional
    public TagResponse getTagByUserIdAndProjectIdAndId(Long userId, Long projectId, Long tagId) {
        return tagRepository.findByUserIdAndProjectIdAndId(userId, projectId, tagId)
                .map(tagMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("해당 태그를 찾지 못했습니다.", tagId));
    }

    /* 태그 생성: 프로젝트 멤버 중 최소 CONTRIBUTOR 이상 권장 */
    @Transactional
    public TagResponse createTag(Long userId, TagRequest request) {
        var pm = projectMemberRepository.findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(() -> new NotAcceptableException("프로젝트 멤버만 생성할 수 있습니다.", userId));
        roleFunc.requireAtLeast(pm.getRole(), RoleProject.CONTRIBUTOR, "태그 생성 권한이 없습니다.", userId);

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new BadRequestException("태그명이 비어있습니다.", request.name());
        }

        // 프로젝트 존재 체크
        var project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", request.projectId()));

        // (project_id, name) 중복 검사
        if (tagRepository.existsByProjectIdAndNameIgnoreCase(project.getId(), request.name().trim())) {
            throw new ConflictException("동일한 이름의 태그가 이미 존재합니다.", request.name());
        }

        Tag tag = Tag.builder()
                .project(project)
                .name(request.name().trim())
                .description(request.description())
                .build();

        Tag savedtag = tagRepository.save(tag);

        activityLogService.log(ActivityEntityType.TAG, ActivityAction.CREATE, savedtag.logTargetId(), savedtag.logMessage(), pm.getUser(), savedtag.logProject(), tag, savedtag);

        return tagMapper.toResponse(tag);
    }

    /* 태그 수정: 프로젝트 멤버 중 최소 CONTRIBUTOR 이상 추천 (정책에 따라 MANAGER 이상으로 격상 가능) */
    @Transactional
    public TagResponse updateTag(Long userId, Long tagId, TagRequest request) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("해당 태그를 찾을 수 없습니다.", tagId));

        var pm = projectMemberRepository.findByUserIdAndProjectId(userId, tag.getProject().getId())
                .orElseThrow(() -> new NotAcceptableException("프로젝트 멤버만 수정할 수 있습니다.", userId));
        roleFunc.requireAtLeast(pm.getRole(), RoleProject.CONTRIBUTOR, "태그 수정 권한이 없습니다.", userId);

        if (request.name() != null && !request.name().trim().isEmpty()) {
            String newName = request.name().trim();
            if (!newName.equalsIgnoreCase(tag.getName()) &&
                    tagRepository.existsByProjectIdAndNameIgnoreCase(tag.getProject().getId(), newName)) {
                throw new ConflictException("동일한 이름의 태그가 이미 존재합니다.", newName);
            }
            tag.setName(newName);
        }
        if (request.description() != null) {
            tag.setDescription(request.description());
        }

        Tag savedtag = tagRepository.save(tag);

        activityLogService.log(ActivityEntityType.TAG, ActivityAction.UPDATE, savedtag.logTargetId(), savedtag.logMessage(), pm.getUser(), savedtag.logProject(), tag, savedtag);

        return tagMapper.toResponse(tag);
    }

    /* 태그 삭제: 일반적으로 MANAGER 이상 */
    @Transactional
    public void deleteTag(Long userId, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("해당 태그를 찾을 수 없습니다.", tagId));

        var pm = projectMemberRepository.findByUserIdAndProjectId(userId, tag.getProject().getId())
                .orElseThrow(() -> new NotAcceptableException("프로젝트 멤버만 삭제할 수 있습니다.", userId));
        roleFunc.requireAtLeast(pm.getRole(), RoleProject.MANAGER, "태그 삭제 권한이 없습니다.", userId);

        activityLogService.log(ActivityEntityType.TAG, ActivityAction.DELETE, tag.logTargetId(), tag.logMessage(), pm.getUser(), tag.logProject(), tag, null);
        // 연결된 TaskTag가 있으면 FK/제약 위반될 수 있습니다.
        // orphanRemoval/ON DELETE CASCADE 설정에 맞춰 예외 처리
        tagRepository.delete(tag);
    }
}
