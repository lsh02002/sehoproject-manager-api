package com.sehoprojectmanagerapi.repository.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByAuthorIdAndId(Long userId, Long commentId);

    void deleteByAuthorIdAndId(Long userId, Long commentId);

    List<Comment> findByTaskId(Long taskId);

    List<Comment> findByAuthorId(Long userId);
}
