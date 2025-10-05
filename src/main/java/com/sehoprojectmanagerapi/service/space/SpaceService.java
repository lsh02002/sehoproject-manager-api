package com.sehoprojectmanagerapi.service.space;

import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRepository;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMemberRepository;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.space.SpaceRequest;
import com.sehoprojectmanagerapi.web.dto.space.SpaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final WorkspaceRepository workspaceRepository;
    private final SpaceRepository spaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

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

        Space space = Space.builder()
                .workspace(workspace)
                .name(request.name())
                .slug(request.slug())
                .build();

        space = spaceRepository.save(space);
        return new SpaceResponse(space.getId(), space.getName(), space.getSlug(), workspace.getId());
    }

    @Transactional(readOnly = true)
    public List<SpaceResponse> listSpaces(Long currentUserId, Long workspaceId) {
        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(currentUserId, workspaceId)) {
            throw new NotAcceptableException("워크스페이스 멤버만 스페이스 목록을 조회할 수 있습니다.", null);
        }

        return spaceRepository.findByWorkspaceId(workspaceId)
                .stream().map(space -> new SpaceResponse(space.getId(), space.getName(), space.getSlug(), space.getWorkspace().getId())).toList();
    }

    @Transactional(readOnly = true)
    public SpaceResponse getSpace(Long currentUserId, Long workspaceId, Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스를 찾을 수 없습니다.", spaceId));

        if (!space.getWorkspace().getId().equals(workspaceId)) {
            throw new ConflictException("워크스페이스-스페이스 소속이 일치하지 않습니다.", spaceId);
        }

        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(currentUserId, workspaceId)) {
            throw new NotAcceptableException("워크스페이스 멤버만 스페이스를 조회할 수 있습니다.", null);
        }

        return new SpaceResponse(space.getId(), space.getName(), space.getSlug(), space.getWorkspace().getId());
    }

    @Transactional
    public SpaceResponse updateSpace(Long currentUserId, Long workspaceId, Long spaceId, SpaceRequest request) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스를 찾을 수 없습니다.", spaceId));

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
        return new SpaceResponse(space.getId(), space.getName(), space.getSlug(), space.getWorkspace().getId());
    }

    @Transactional
    public void deleteSpace(Long currentUserId, Long workspaceId, Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundException("스페이스를 찾을 수 없습니다.", spaceId));

        if (!space.getWorkspace().getId().equals(workspaceId)) {
            throw new ConflictException("워크스페이스-스페이스 소속이 일치하지 않습니다.", spaceId);
        }

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(currentUserId, workspaceId)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스 멤버만 삭제할 수 있습니다.", null));
        if (role != WorkspaceRole.OWNER) {
            throw new NotAcceptableException("OWNER만 스페이스를 삭제할 수 있습니다.", null);
        }

        spaceRepository.delete(space);
    }
}
