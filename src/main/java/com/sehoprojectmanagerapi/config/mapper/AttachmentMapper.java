package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.attachment.Attachment;
import com.sehoprojectmanagerapi.web.dto.attachment.AttachmentResponse;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {
    public AttachmentResponse toResponse(Attachment a) {
        return AttachmentResponse.builder()
                .id(a.getId())
                .taskId(a.getTask().getId())
                .uploaderId(a.getUploader().getId())
                .fileName(a.getFileName())
                .fileUrl(a.getFileUrl())
                .mimeType(a.getMimeType())
                .sizeBytes(a.getSizeBytes())
                .build();
    }
}
