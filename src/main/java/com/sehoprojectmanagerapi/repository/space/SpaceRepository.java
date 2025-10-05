package com.sehoprojectmanagerapi.repository.space;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SpaceRepository extends JpaRepository<Space, Long> {
    boolean existsByWorkspaceIdAndSlug(Long workspaceId, String slug);
    List<Space> findByWorkspaceId(Long workspaceId);
}
