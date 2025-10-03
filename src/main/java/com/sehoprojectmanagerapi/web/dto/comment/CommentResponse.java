package com.sehoprojectmanagerapi.web.dto.comment;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record CommentResponse(
        Long commentId,
        Long taskId,
        Long authorId,
        String authorName,
        String content,
        String avatarUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
