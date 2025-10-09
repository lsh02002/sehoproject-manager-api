package com.sehoprojectmanagerapi.repository.task.taskassignee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, String> {
    void deleteByTaskId(Long taskId);

    @Modifying
    @Transactional
    @Query("""
        update TaskAssignee ta
           set ta.dynamic = :dynamic
         where ta.task.id = :taskId
           and ta.assigneeType = 'TEAM'
    """)
    void updateDynamicFlagIfTeam(Long taskId, boolean dynamic);
}
