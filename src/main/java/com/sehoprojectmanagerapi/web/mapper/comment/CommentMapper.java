package com.sehoprojectmanagerapi.web.mapper.comment;

import com.sehoprojectmanagerapi.repository.comment.Comment;
import com.sehoprojectmanagerapi.web.dto.comment.CommentResponse;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .taskId(comment.getTask().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getNickname())
                .avatarUrl(comment.getAuthor().getAvatarUrl())
                .content(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
