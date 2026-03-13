package com.sehoprojectmanagerapi.repository.sprint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    @Query("""
    select distinct s
      from Sprint s
      join s.project p
     where p.id = :projectId
       and exists (
            select 1
              from Task t
              join t.assignees ta
             where t.sprint = s
               and ta.assigneeType = com.sehoprojectmanagerapi.repository.task.taskassignee.AssigneeType.USER
               and ta.assigneeId = :userId
       )
""")
    List<Sprint> findAllVisibleForUser(Long userId, Long projectId);

    List<Sprint> findByProjectId(Long projectId);
}
