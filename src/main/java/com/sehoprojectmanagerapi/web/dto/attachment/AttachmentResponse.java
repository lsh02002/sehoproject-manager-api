package com.sehoprojectmanagerapi.web.dto.attachment;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AttachmentResponse {
    Long id;
    Long taskId;
    Long uploaderId;
    String fileName;
    String fileUrl;
    String mimeType;
    Long sizeBytes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
