package com.sehoprojectmanagerapi.web.mapper.taskimage;

import com.sehoprojectmanagerapi.config.s3.S3Address;
import com.sehoprojectmanagerapi.repository.attachment.Attachment;
import com.sehoprojectmanagerapi.web.dto.attachment.AttachmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskImageMapper {
    private final S3Address s3Address;

    public AttachmentResponse toResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .taskId(attachment.getTask() != null ? attachment.getTask().getId() : null)
                .uploaderId(attachment.getUploader() != null ? attachment.getUploader().getId() : null)
                .fileName(attachment.getFileName())
                .fileUrl(s3Address.siteAddress() + attachment.getFileUrl())
                .deleted(attachment.getDeleted())
                .build();
    }
}