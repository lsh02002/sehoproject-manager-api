package com.sehoprojectmanagerapi.repository.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);

    List<Tag> findAllByProjectId(Long projectId);

    @Query("""
                select t
                  from Tag t
                 where t.project.id = :projectId
                   and t.id = :tagId
            """)
    Optional<Tag> findByProjectIdAndId(@Param("projectId") Long projectId, @Param("tagId") Long tagId);

    @Query("""
                select t
                  from Tag t
                  join t.project p
                  join ProjectMember pm on pm.project.id = p.id
                 where pm.user.id = :userId
                   and p.id = :projectId
                   and t.id = :tagId
            """)
    Optional<Tag> findByUserIdAndProjectIdAndId(@Param("userId") Long userId,
                                                @Param("projectId") Long projectId,
                                                @Param("tagId") Long tagId);

    Optional<Tag> findByNameAndProjectId(String name, Long projectId);

    void deleteByNameAndProjectId(String name, Long projectId);
}
