package com.sehoprojectmanagerapi.web.controller.attachment;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.attachment.AttachmentService;
import com.sehoprojectmanagerapi.web.dto.attachment.AttachmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attachments/")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(path = "/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadFile(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long taskId, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(attachmentService.uploadFile(customUserDetails.getId(), taskId, file));
    }

    @PostMapping(path = "/{taskId}/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<AttachmentResponse>> uploadManyFile(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long taskId, @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(attachmentService.uploadManyFile(customUserDetails.getId(), taskId, files));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponse> findAttachmentById(@PathVariable Long attachmentId) {
        return ResponseEntity.ok(attachmentService.findAttachmentById(attachmentId));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachmentById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long attachmentId) {
        attachmentService.deleteFile(customUserDetails.getId(), attachmentId);
        return ResponseEntity.ok().build();
    }
}
