package com.sehoprojectmanagerapi.web.dto.comment;

public record CommentRequest(
        Long taskId,
        String content
) {
}

