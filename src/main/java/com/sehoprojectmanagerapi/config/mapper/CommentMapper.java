package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.comment.Comment;
import com.sehoprojectmanagerapi.web.dto.comment.CommentResponse;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class CommentMapper {
    public CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .taskId(comment.getTask().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .avatarUrl(comment.getAuthor().getAvatarUrl())
                .content(comment.getBody())
                .createdAt(OffsetDateTime.from(comment.getCreatedAt()))
                .updatedAt(OffsetDateTime.from(comment.getUpdatedAt()))
                .build();
    }
}
