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
                  left join p.teams t
                  left join t.members tm
                 where tm.user.id = :userId
                    or p.createdBy.id = :userId
            """)
    List<Milestone> findAllVisibleForUser(@Param("userId") Long userId);
}
