package com.sehoprojectmanagerapi.repository.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("""
    select p
      from Project p
      join p.teams t
     where t.id = :teamId
    """)
    List<Project> findAllByTeamId(@Param("teamId") Long teamId);
}
