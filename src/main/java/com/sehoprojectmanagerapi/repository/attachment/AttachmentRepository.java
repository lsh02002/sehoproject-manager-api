package com.sehoprojectmanagerapi.repository.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByUploaderIdAndId(Long userId, Long attachmentId);
}
