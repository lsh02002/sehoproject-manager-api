package com.sehoprojectmanagerapi.service.workspace;

import com.sehoprojectmanagerapi.web.mapper.WorkspaceMapper;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRepository;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMember;
import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMemberRepository;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.workspace.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;

    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, WorkspaceRequest request) {
        if (workspaceRepository.existsBySlug(request.slug())) {
            throw new ConflictException("중복된 워크스페이스 슬러그입니다.", request.slug());
        }

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", userId));

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .slug(request.slug())
                .createdBy(creator)
                .build();
        workspace = workspaceRepository.save(workspace);

        // 작성자 = OWNER 로 멤버십 자동 생성
        WorkspaceMember owner = WorkspaceMember.builder()
                .workspace(workspace)
                .user(creator)
                .role(WorkspaceRole.OWNER)
                .joinedAt(OffsetDateTime.now())
                .build();
        workspaceMemberRepository.save(owner);

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> listWorkspaces(Long userId) {
        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByUserId(userId);

        return workspaceMembers
                .stream().map(member->workspaceMapper.toResponse(member.getWorkspace())).toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long userId, Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(userId, id)) {
            throw new NotAcceptableException("워크스페이스 멤버만 조회할 수 있습니다.", null);
        }

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long userId, Long id, WorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(userId, id)
                .orElseThrow(() -> new NotAcceptableException("워크스페이스 멤버만 수정할 수 있습니다.", null));
        if (!(role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN)) {
            throw new NotAcceptableException("OWNER 또는 ADMIN만 수정할 수 있습니다.", null);
        }

        if (!workspace.getSlug().equals(request.slug()) && workspaceRepository.existsBySlug(request.slug())) {
            throw new ConflictException("중복된 워크스페이스 슬러그입니다.", request.slug());
        }

        workspace.setName(request.name());
        workspace.setSlug(request.slug());

        workspace = workspaceRepository.save(workspace);

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public void deleteWorkspace(Long currentUserId, Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("워크스페이스를 찾을 수 없습니다.", id));

        var role = workspaceMemberRepository.findRoleByUserIdAndWorkspaceId(currentUserId, id)
                .orElseThrow(() -> new BadCredentialsException("워크스페이스 멤버만 삭제할 수 있습니다.", null));
        if (role != WorkspaceRole.OWNER) {
            throw new BadCredentialsException("OWNER만 워크스페이스를 삭제할 수 있습니다.", null);
        }

        workspaceRepository.delete(workspace);
    }
}
