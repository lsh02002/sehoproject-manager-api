package com.sehoprojectmanagerapi.repository.space;

import com.sehoprojectmanagerapi.web.dto.workspace.TreeRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface SpaceRepository extends JpaRepository<Space, Long> {
    boolean existsByWorkspaceIdAndSlug(Long workspaceId, String slug);
    List<Space> findByWorkspaceId(Long workspaceId);

    // 단일 워크스페이스용
    @Query("""
        select new com.sehoprojectmanagerapi.web.dto.workspace.TreeRow(
            w.id,
            s.id, s.name,
            p.id, p.name
        )
        from Space s
        join s.workspace w
        left join Project p on p.space.id = s.id
        where w.id = :workspaceId
        order by s.position asc, s.id asc, p.position asc, p.id asc
    """)
    List<TreeRow> findTreeRowsByWorkspaceId(@Param("workspaceId") Long workspaceId);

    // 여러 워크스페이스용 (이번에 추가)
    @Query("""
        select new com.sehoprojectmanagerapi.web.dto.workspace.TreeRow(
            w.id,
            s.id, s.name,
            p.id, p.name
        )
        from Space s
        join s.workspace w
        left join Project p on p.space.id = s.id
        where w.id in :workspaceIds
        order by w.position asc, w.id asc,
                 s.position asc, s.id asc,
                 p.position asc, p.id asc
    """)
    List<TreeRow> findTreeRowsByWorkspaceIds(@Param("workspaceIds") List<Long> workspaceIds);
}
