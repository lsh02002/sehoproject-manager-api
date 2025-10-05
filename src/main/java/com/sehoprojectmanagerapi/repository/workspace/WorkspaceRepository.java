package com.sehoprojectmanagerapi.repository.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    boolean existsBySlug(String slug);
}
