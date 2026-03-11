package com.sehoprojectmanagerapi.service.attachment;

import com.sehoprojectmanagerapi.config.function.RoleFunc;
import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.attachment.Attachment;
import com.sehoprojectmanagerapi.repository.attachment.AttachmentRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.TaskRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.attachment.AttachmentResponse;
import com.sehoprojectmanagerapi.web.dto.attachment.FileRequest;
import com.sehoprojectmanagerapi.web.mapper.attachment.AttachmentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final ActivityLogService activityLogService;
    private final S3StorageService s3storageService;
    private final AttachmentMapper attachmentMapper;
    private final SnapshotFunc snapshotFunc;
    private final ProjectMemberRepository projectMemberRepository;
    private final RoleFunc roleFunc;

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

        task.getTaskImages().add(saved);

        Object aftertaskimage = snapshotFunc.snapshot(saved);

        activityLogService.log(ActivityEntityType.ATTACHMENT, ActivityAction.CREATE, saved.getId(), saved.logMessage(), uploader, null, aftertaskimage);

        return attachmentMapper.toResponse(saved);
    }

    public AttachmentResponse uploadFile(User uploader, Task task, MultipartFile file) {
        validateFile(file);

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

        task.getTaskImages().add(saved);

        Object aftertaskimage = snapshotFunc.snapshot(saved);

        activityLogService.log(ActivityEntityType.ATTACHMENT, ActivityAction.CREATE, saved.getId(), saved.logMessage(), uploader, null, aftertaskimage);

        return attachmentMapper.toResponse(saved);
    }

    public List<AttachmentResponse> uploadManyFiles(Long userId, Long taskId, List<MultipartFile> files) {
        List<AttachmentResponse> responses = new ArrayList<>();
        if (files == null || files.isEmpty()) return responses;

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 Task입니다. id=", taskId));
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id=", userId));

        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(task.getProject().getId());

        boolean hasPermission = projectMembers.stream()
                .anyMatch(pm ->
                        pm.getUser().getId().equals(uploader.getId()) &&
                                roleFunc.hasAtLeast(pm.getRole(), RoleProject.CONTRIBUTOR)
                );

        if(!hasPermission) {
            throw new NotAcceptableException("해당 사용자는 이미지 업로드 권한이 없습니다!", null);
        }

        List<Attachment> currentImages = task.getTaskImages().stream()
                .filter(image -> !image.getDeleted())
                .toList();

        for (Attachment attachment : currentImages) {
            boolean existsInRequest = files.stream().anyMatch(file ->
                    Objects.equals(attachment.getFileName(), file.getOriginalFilename())
                            && attachment.getSizeBytes() == file.getSize()
            );

            if (!existsInRequest) {
                deleteFile(uploader, attachment);
            }
        }

        for (MultipartFile file : files) {
            boolean existsInDb = currentImages.stream().anyMatch(image ->
                    Objects.equals(image.getFileName(), file.getOriginalFilename())
                            && image.getSizeBytes() == file.getSize()
            );

            if (!existsInDb) {
                responses.add(uploadFile(uploader, task, file));
            }
        }
        return responses;
    }

    public void deleteFile(Long userId, Long attachmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id=", userId));

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(()->new NotFoundException("해당 이미지가 존재하지 않습니다. ", attachmentId));

        if(!attachment.getDeleted()) {
            attachment.setDeleted(true);
        }

        Object afterattachimage = snapshotFunc.snapshot(attachment);

        activityLogService.log(ActivityEntityType.ATTACHMENT, ActivityAction.DELETE, attachment.getId(), attachment.logMessage(), user, null, afterattachimage);
    }

    public void deleteFile(User user, Attachment attachment) {
        if(!attachment.getDeleted()) {
            attachment.setDeleted(true);
        }

        Object afterattachimage = snapshotFunc.snapshot(attachment);

        activityLogService.log(ActivityEntityType.ATTACHMENT, ActivityAction.DELETE, attachment.getId(), attachment.logMessage(), user, null, afterattachimage);
    }

    public void deleteFileByUserAndTaskImage(User user, Attachment image) {
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(image.getTask().getProject().getId());

        boolean hasPermission = projectMembers.stream()
                .anyMatch(pm ->
                        pm.getUser().getId().equals(user.getId()) &&
                                roleFunc.hasAtLeast(pm.getRole(), RoleProject.CONTRIBUTOR)
                );

        if(!hasPermission) {
            throw new NotAcceptableException("해당 사용자는 이미지 삭제 권한이 없습니다!", null);
        }

        Object beforeattachimage = snapshotFunc.snapshot(image);

        // fileUrl에서 저장 파일명을 추출할 수 있게 해두었다면 여기서 삭제
        // 단순하게 storedFileName만 별도 칼럼으로 저장하는 방법을 권장합니다.
        // 예시로 fileUrl 마지막 토큰을 파일명으로 가정:
        String storedFileName = extractStoredFileName(image.getFileUrl());
        // s3storageService.deleteFile(storedFileName);
        if(!image.getDeleted()) {
            image.setDeleted(true);
        }

        // diaryImageRepository.delete(entity);

        Object afterattachimage = snapshotFunc.snapshot(image);

    activityLogService.log(ActivityEntityType.ATTACHMENT, ActivityAction.DELETE, image.getId(), image.logMessage(), user, beforeattachimage, afterattachimage);
    }

    public void deleteManyFiles(User user, Task task) {
        for (Attachment image: task.getTaskImages()) {
            if(image.getDeleted() == true) {
                continue;
            }

            deleteFileByUserAndTaskImage(user, image);
        }
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
