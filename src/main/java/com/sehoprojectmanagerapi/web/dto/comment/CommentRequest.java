package com.sehoprojectmanagerapi.web.dto.comment;

import lombok.Builder;

@Builder
public record CommentRequest(
        Long taskId,
        String content
) {
}

