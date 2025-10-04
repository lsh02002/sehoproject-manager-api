package com.sehoprojectmanagerapi.repository.sprint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    @Query("""
        select distinct s
          from Sprint s
          join s.project p
         where p.createdBy.id = :userId
            or exists (
                select 1
                  from com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember pm
                 where pm.project = p
                   and pm.user.id = :userId
            )
    """)
    List<Sprint> findAllVisibleForUser(Long userId);
}
