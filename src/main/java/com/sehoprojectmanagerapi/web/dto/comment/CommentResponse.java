package com.sehoprojectmanagerapi.web.dto.comment;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Builder
public record CommentResponse(
        Long commentId,
        Long taskId,
        Long authorId,
        String authorName,
        String content,
        String avatarUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
