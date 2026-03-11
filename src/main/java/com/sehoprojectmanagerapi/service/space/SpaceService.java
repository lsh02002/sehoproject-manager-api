package com.sehoprojectmanagerapi.service.space;

import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.repository.space.SpaceRole;
import com.sehoprojectmanagerapi.repository.space.spacemember.SpaceMember;
import com.sehoprojectmanagerapi.repository.space.spacemember.SpaceMemberRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRepository;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMemberRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.space.SpaceRequest;
import com.sehoprojectmanagerapi.web.dto.space.SpaceResponse;
import com.sehoprojectmanagerapi.web.mapper.SpaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final WorkspaceRepository workspaceRepository;
    private final SpaceRepository spaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final SpaceMapper spaceMapper;
    private final UserRepository userRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final ActivityLogService activityLogService;
    private final SnapshotFunc snapshotFunc;

    @Transactional
    public SpaceResponse createSpace(Long currentUserId, Long workspaceId, SpaceRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", workspaceId));

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(currentUserId, workspaceId)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스 멤버만 스페이스를 생성할 수 있습니다.", null));
        if (!(role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN)) {
            throw new NotAcceptableException("OWNER 또는 ADMIN만 스페이스를 생성할 수 있습니다.", null);
        }

        if (spaceRepository.existsByWorkspaceIdAndSlug(workspaceId, request.slug())) {
            throw new ConflictException("해당 워크스페이스 내에서 중복된 스페이스 슬러그입니다.", request.slug());
        }

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", currentUserId));

        Space space = Space.builder()
                .workspace(workspace)
                .name(request.name())
                .slug(request.slug())
                .createdBy(creator)
                .build();

        space = spaceRepository.save(space);

        Object afterspace = snapshotFunc.snapshot(space);

        // 작성자 = ADMIN 로 멤버십 자동 생성
        SpaceMember spaceOwner = SpaceMember.builder()
                .space(space)
                .user(creator)
                .role(SpaceRole.ADMIN)
                .joinedAt(OffsetDateTime.now())
                .build();
        spaceMemberRepository.save(spaceOwner);

        Object afterspacemember = snapshotFunc.snapshot(spaceOwner);

        activityLogService.log(ActivityEntityType.SPACE_MEMBER, ActivityAction.CREATE, spaceOwner.getId(), spaceOwner.logMessage(), creator, null, afterspacemember);

        activityLogService.log(ActivityEntityType.SPACE, ActivityAction.CREATE, space.getId(), space.logMessage(), creator, null, afterspace);

        return spaceMapper.toResponse(space);
    }

    @Transactional
    public List<SpaceResponse> listSpaces(Long currentUserId, Long workspaceId) {
        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(currentUserId, workspaceId)) {
            throw new NotAcceptableException("워크스페이스 멤버만 스페이스 목록을 조회할 수 있습니다.", null);
        }

        return spaceRepository.findByWorkspaceId(workspaceId)
                .stream().map(spaceMapper::toResponse).toList();
    }

    @Transactional
    public SpaceResponse getSpace(Long currentUserId, Long workspaceId, Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스를 찾을 수 없습니다.", spaceId));

        if (!space.getWorkspace().getId().equals(workspaceId)) {
            throw new ConflictException("워크스페이스-스페이스 소속이 일치하지 않습니다.", spaceId);
        }

        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(currentUserId, workspaceId)) {
            throw new NotAcceptableException("워크스페이스 멤버만 스페이스를 조회할 수 있습니다.", null);
        }

        return spaceMapper.toResponse(space);
    }

    @Transactional
    public SpaceResponse updateSpace(Long currentUserId, Long workspaceId, Long spaceId, SpaceRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new NotFoundException("해당 유저를 찾을 수 없습니다.", currentUserId));

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스를 찾을 수 없습니다.", spaceId));

        Object beforespace = snapshotFunc.snapshot(space);

        if (!space.getWorkspace().getId().equals(workspaceId)) {
            throw new ConflictException("워크스페이스-스페이스 소속이 일치하지 않습니다.", spaceId);
        }

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(currentUserId, workspaceId)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스 멤버만 수정할 수 있습니다.", null));
        if (!(role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN)) {
            throw new NotAcceptableException("OWNER 또는 ADMIN만 스페이스를 수정할 수 있습니다.", null);
        }

        if (!space.getSlug().equals(request.slug()) &&
                spaceRepository.existsByWorkspaceIdAndSlug(workspaceId, request.slug())) {
            throw new ConflictException("해당 워크스페이스 내에서 중복된 스페이스 슬러그입니다.", request.slug());
        }

        space.setName(request.name());
        space.setSlug(request.slug());

        Object afterspace = snapshotFunc.snapshot(space);

        activityLogService.log(
                ActivityEntityType.SPACE, ActivityAction.UPDATE,
                space.getId(), space.logMessage(),
                user,
                beforespace, afterspace
        );

        return spaceMapper.toResponse(space);
    }

    @Transactional
    public void deleteSpace(Long currentUserId, Long workspaceId, Long spaceId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new NotFoundException("해당 유저를 찾을 수 없습니다.", currentUserId));

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스를 찾을 수 없습니다.", spaceId));

        Object beforespace = snapshotFunc.snapshot(space);

        if (!space.getWorkspace().getId().equals(workspaceId)) {
            throw new ConflictException("워크스페이스-스페이스 소속이 일치하지 않습니다.", spaceId);
        }

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(currentUserId, workspaceId)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스 멤버만 삭제할 수 있습니다.", null));
        if (!(role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN)) {
            throw new NotAcceptableException("OWNER 또는 ADMIN만 스페이스를 삭제할 수 있습니다.", null);
        }

        activityLogService.log(ActivityEntityType.SPACE, ActivityAction.DELETE, space.getId(), space.logMessage(), user, beforespace, null);

        spaceRepository.delete(space);
    }
}
