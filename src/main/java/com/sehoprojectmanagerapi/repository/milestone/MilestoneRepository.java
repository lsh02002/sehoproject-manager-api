package com.sehoprojectmanagerapi.repository.milestone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    @Query("""
    select distinct m
      from Milestone m
      join m.project p
     where p.id = :projectId
       and (
            p.createdBy.id = :userId
         or exists (
                select 1
                  from ProjectMember pm
                 where pm.project = p
                   and pm.user.id = :userId
            )
       )
""")
    List<Milestone> findAllVisibleForUserAndProject(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId
    );
}
