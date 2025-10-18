package com.sehoprojectmanagerapi.repository.workspace.workspacemember;

import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    List<WorkspaceMember> findByUserId(Long userId);

    // ✅ 오타 수정: useId -> userId
    boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    // ✅ 멤버의 role만 단일 필드로 조회 (ACTIVE 멤버만)
    @Query("""
                select wm.role
                  from WorkspaceMember wm
                 where wm.user.id = :userId
                   and wm.workspace.id = :workspaceId
                   and wm.status = com.sehoprojectmanagerapi.repository.common.CommonStatus.ACTIVE
            """)
    Optional<WorkspaceRole> findRoleByUserIdAndWorkspaceId(@Param("userId") Long userId,
                                                           @Param("workspaceId") Long workspaceId);

    // ✅ (안전 대안) 엔티티로 조회 후 서비스에서 getRole() 호출 ― JPQL 검증 이슈 방지용
    @Query("""
                select wm
                  from WorkspaceMember wm
                 where wm.user.id = :userId
                   and wm.workspace.id = :workspaceId
                   and wm.status = com.sehoprojectmanagerapi.repository.common.CommonStatus.ACTIVE
            """)
    Optional<WorkspaceMember> findActiveByUserIdAndWorkspaceId(@Param("userId") Long userId,
                                                               @Param("workspaceId") Long workspaceId);

    // 유저가 속한 워크스페이스 ID 목록
    @Query("""
                select wm.workspace.id
                from WorkspaceMember wm
                where wm.user.id = :userId
                order by wm.workspace.position asc, wm.workspace.id asc
            """)
    List<Long> findWorkspaceIdsByUserId(@Param("userId") Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    @Query("""
        select wm.role
        from WorkspaceMember wm
        where wm.workspace.id = :workspaceId
          and wm.user.id = :userId
    """)
    Optional<WorkspaceRole> findRole(@Param("workspaceId") Long workspaceId,
                                     @Param("userId") Long userId);
}
