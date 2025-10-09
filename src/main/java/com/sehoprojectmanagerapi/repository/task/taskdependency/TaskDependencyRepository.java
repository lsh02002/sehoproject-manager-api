package com.sehoprojectmanagerapi.repository.task.taskdependency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, String> {
    void deleteByTaskId(Long id);

    @Query("""
        select case when count(td) > 0 then true else false end
          from TaskDependency td
         where td.task.id in :dependencyTaskIds
           and td.dependsOn.id = :taskId
    """)
    boolean existsMutualDependency(Long taskId, List<Long> dependencyTaskIds);
}
