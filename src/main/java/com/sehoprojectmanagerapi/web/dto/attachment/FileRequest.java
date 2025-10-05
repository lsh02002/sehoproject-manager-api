package com.sehoprojectmanagerapi.web.dto.attachment;

import lombok.Builder;

/**
 * @param storedFileName 물리 저장명 (UUID 등)
 * @param url            접근 URL (예: /files/{storedFileName})
 */
@Builder
public record FileRequest(
        String originalFileName,
        String storedFileName,
        String storedKey,
        String url,
        String mimeType,
        long sizeBytes
) {
}
