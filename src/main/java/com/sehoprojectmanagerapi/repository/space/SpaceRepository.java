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
    w.id, w.position,
    s.id, s.name, s.position,
    p.id, p.name, p.position,

    /* Milestone 필드 */
    m.id, m.name, m.position,

    /* Task 필드 */
    t.id, t.name, t.position,

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
     or exists (
        select 1 from ProjectMember pm
        where pm.project = p
          and pm.user.id = :userId
     )
     or exists (
        select 1 from SpaceMember sm2
        where sm2.space = s
          and sm2.user.id = :userId
          and sm2.role in :rolesGrantingProject
     )
     or exists (
        select 1 from WorkspaceMember wm3
        where wm3.workspace = w
          and wm3.user.id = :userId
          and wm3.role in :rolesGrantingProject
     )),

    /* 마일스톤 접근 가능 여부 (m이 null이면 true) */
    (m is null
     or exists (
        select 1 from ProjectMember pm4
        where pm4.project = p
          and pm4.user.id = :userId
     )
     or exists (
        select 1 from SpaceMember sm4
        where sm4.space = s
          and sm4.user.id = :userId
          and sm4.role in :rolesGrantingProject
     )
     or exists (
        select 1 from WorkspaceMember wm4
        where wm4.workspace = w
          and wm4.user.id = :userId
          and wm4.role in :rolesGrantingProject
     )),

    /* 태스크 접근 가능 여부 (t가 null이면 true) */
    (t is null
     or exists (
        select 1 from ProjectMember pm5
        where pm5.project = p
          and pm5.user.id = :userId
     )
     or exists (
        select 1 from SpaceMember sm5
        where sm5.space = s
          and sm5.user.id = :userId
          and sm5.role in :rolesGrantingProject
     )
     or exists (
        select 1 from WorkspaceMember wm5
        where wm5.workspace = w
          and wm5.user.id = :userId
          and wm5.role in :rolesGrantingProject
     ))
)
from Space s
join s.workspace w
left join Project p on p.space = s
left join Milestone m on m.project = p
left join Task t on t.project = p
where w.id in :workspaceIds
order by
    w.position asc, w.id asc,
    s.position asc, s.id asc,
    p.position asc, p.id asc,
    m.position asc, m.id asc,
    t.position asc, t.id asc
""")
    List<TreeRow> findTreeRowsVisibleToUser(
            @Param("workspaceIds") Collection<Long> workspaceIds,
            @Param("userId") Long userId,
            @Param("rolesGrantingSpace") Collection<Role> rolesGrantingSpace,
            @Param("rolesGrantingProject") Collection<Role> rolesGrantingProject
    );

}
