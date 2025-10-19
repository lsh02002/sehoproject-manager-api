package com.sehoprojectmanagerapi.service.attachment;

import com.sehoprojectmanagerapi.repository.attachment.Attachment;
import com.sehoprojectmanagerapi.repository.attachment.AttachmentRepository;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.TaskRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.attachment.AttachmentResponse;
import com.sehoprojectmanagerapi.web.dto.attachment.FileRequest;
import com.sehoprojectmanagerapi.web.mapper.AttachmentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    // 정책 값 (필요시 @Value로 주입)
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_MIME_PREFIXES = List.of(
            "image/", "text/", "application/pdf", "application/zip"
    );
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3storageService;
    private final AttachmentMapper attachmentMapper;

    public AttachmentResponse uploadFile(Long userId, Long taskId, MultipartFile file) {
        validateFile(file);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 Task입니다. id=", taskId));
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id=", userId));

        FileRequest stored = s3storageService.saveFile(file);

        Attachment saved = attachmentRepository.save(
                Attachment.builder()
                        .task(task)
                        .uploader(uploader)
                        .fileName(stored.originalFileName())
                        .fileUrl(stored.url())
                        .mimeType(stored.mimeType())
                        .sizeBytes(stored.sizeBytes())
                        .build()
        );

        return attachmentMapper.toResponse(saved);
    }

    public List<AttachmentResponse> uploadManyFile(Long userId, Long taskId, List<MultipartFile> files) {
        List<AttachmentResponse> responses = new ArrayList<>();
        if (files == null || files.isEmpty()) return responses;

        for (MultipartFile file : files) {
            responses.add(uploadFile(userId, taskId, file));
        }
        return responses;
    }

    public void deleteFile(Long userId, Long attachmentId) {
        Attachment entity = attachmentRepository.findByUploaderIdAndId(userId, attachmentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다. id=", attachmentId));

        // fileUrl에서 저장 파일명을 추출할 수 있게 해두었다면 여기서 삭제
        // 단순하게 storedFileName만 별도 칼럼으로 저장하는 방법을 권장합니다.
        // 예시로 fileUrl 마지막 토큰을 파일명으로 가정:
        String storedFileName = extractStoredFileName(entity.getFileUrl());
        s3storageService.deleteFile(storedFileName);

        attachmentRepository.delete(entity);
    }

    public AttachmentResponse findAttachmentById(Long attachmentId) {
        Attachment entity = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다. id=", attachmentId));
        return attachmentMapper.toResponse(entity);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new NotAcceptableException("빈 파일은 업로드할 수 없습니다.", null);
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new NotAcceptableException("파일이 너무 큽니다. 최대 ", (MAX_SIZE_BYTES / (1024 * 1024)) + "MB");
        }
        String mime = file.getContentType();
        if (mime == null) {
            throw new ConflictException("지원하지 않는 파일 형식입니다. (MIME 미확인)", null);
        }
        boolean allowed = ALLOWED_MIME_PREFIXES.stream().anyMatch(prefix ->
                (prefix.endsWith("/") && mime.startsWith(prefix)) || mime.equals(prefix)
        );
        if (!allowed) {
            throw new ConflictException("지원하지 않는 파일 형식입니다. type=", mime);
        }
        // (선택) 실행파일/스크립트 차단: application/x-msdownload 등
        if ("application/octet-stream".equals(mime) && file.getOriginalFilename() != null) {
            String name = file.getOriginalFilename().toLowerCase();
            if (name.endsWith(".exe") || name.endsWith(".sh") || name.endsWith(".bat")) {
                throw new NotAcceptableException("실행 파일 업로드는 허용되지 않습니다.", null);
            }
        }
    }

    private String extractStoredFileName(String fileUrl) {
        if (fileUrl == null) return null;
        int idx = fileUrl.lastIndexOf('/');
        return (idx >= 0 && idx + 1 < fileUrl.length()) ? fileUrl.substring(idx + 1) : fileUrl;
    }
}
