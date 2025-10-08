package com.sehoprojectmanagerapi.repository.space;

import com.sehoprojectmanagerapi.web.dto.workspace.TreeRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface SpaceRepository extends JpaRepository<Space, Long> {
    boolean existsByWorkspaceIdAndSlug(Long workspaceId, String slug);
    List<Space> findByWorkspaceId(Long workspaceId);

    @Query("""
    select new com.sehoprojectmanagerapi.web.dto.workspace.TreeRow(
        s.id, s.name,
        p.id, p.name
    )
    from Space s
    left join s.projects p
    where s.workspace.id = :workspaceId
    order by s.name asc, p.name asc
    """)
    List<TreeRow> findTreeRowsByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
