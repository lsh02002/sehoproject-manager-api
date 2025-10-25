package com.sehoprojectmanagerapi.repository.space;

import com.sehoprojectmanagerapi.repository.common.Role;
import com.sehoprojectmanagerapi.web.dto.workspace.TreeRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    boolean existsByWorkspaceIdAndSlug(Long workspaceId, String slug);

    List<Space> findByWorkspaceId(Long workspaceId);

    @Query("""
            select distinct new com.sehoprojectmanagerapi.web.dto.workspace.TreeRow(
                w.id, w.name, w.position,
                s.id, s.name, s.position,
                p.id, p.name, p.position,
            
                /* Milestone */
                m.id, m.name, m.position,
            
                /* Sprint */
                sp.id, sp.name, sp.position,
            
                /* 워크스페이스 접근 가능 여부 */
                (exists (
                    select 1 from WorkspaceMember wm
                    where wm.workspace = w
                      and wm.user.id = :userId
                )),
            
                /* 스페이스 접근 가능 여부 */
                (exists (
                    select 1 from SpaceMember sm
                    where sm.space = s
                      and sm.user.id = :userId
                )
                or exists (
                    select 1 from WorkspaceMember wm2
                    where wm2.workspace = w
                      and wm2.user.id = :userId
                      and wm2.role in :rolesGrantingSpace
                )),
            
                /* 프로젝트 접근 가능 여부 (p가 null이면 true) */
                (p is null
                 or pm_u.id is not null
                 or sm_proj.id is not null
                 or wm_proj.id is not null),
            
                /* 마일스톤 접근 가능 여부 (m이 null이면 true) */
                (m is null
                 or pm_u.id is not null
                 or sm_proj.id is not null
                 or wm_proj.id is not null),
            
                /* 스프린트 접근 가능 여부 (sp가 null이면 true) */
                (sp is null
                 or pm_u.id is not null
                 or sm_proj.id is not null
                 or wm_proj.id is not null)
            )
            from Space s
            join s.workspace w
            left join Project p on p.space = s
            left join Milestone m on m.project = p
            left join Sprint sp on sp.project = p
            
            /* ===== 공통 권한 조인(재사용) ===== */
            left join ProjectMember pm_u
                   on pm_u.project = p
                  and pm_u.user.id = :userId
            
            left join SpaceMember sm_proj
                   on sm_proj.space = s
                  and sm_proj.user.id = :userId
                  and sm_proj.role in :rolesGrantingProject
            
            left join WorkspaceMember wm_proj
                   on wm_proj.workspace = w
                  and wm_proj.user.id = :userId
                  and wm_proj.role in :rolesGrantingProject
            /* ================================= */
            
            where w.id in :workspaceIds
            order by
                w.position asc, w.id asc,
                s.position asc, s.id asc,
                p.position asc, p.id asc,
                m.position asc, m.id asc,
                sp.position asc, sp.id asc
            """)
    List<TreeRow> findTreeRowsVisibleToUser(
            @Param("workspaceIds") Collection<Long> workspaceIds,
            @Param("userId") Long userId,
            @Param("rolesGrantingSpace") Collection<Role> rolesGrantingSpace,
            @Param("rolesGrantingProject") Collection<Role> rolesGrantingProject
    );
}
