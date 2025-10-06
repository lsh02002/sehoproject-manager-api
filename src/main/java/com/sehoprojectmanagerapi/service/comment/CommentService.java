package com.sehoprojectmanagerapi.service.comment;

import com.sehoprojectmanagerapi.web.mapper.CommentMapper;
import com.sehoprojectmanagerapi.repository.comment.Comment;
import com.sehoprojectmanagerapi.repository.comment.CommentRepository;
import com.sehoprojectmanagerapi.repository.task.Task;
import com.sehoprojectmanagerapi.repository.task.TaskRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.comment.CommentRequest;
import com.sehoprojectmanagerapi.web.dto.comment.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentByTaskId(Long taskId) {
        return commentRepository.findByTaskId(taskId)
                .stream().map(commentMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        return commentMapper.toResponse(comment);
    }

    @Transactional
    public List<CommentResponse> getCommentsByUserId(Long userId) {
        return commentRepository.findByAuthorId(userId)
                .stream().map(commentMapper::toResponse).toList();
    }

    @Transactional
    public CommentResponse createComment(Long userId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new NotFoundException("해당 테스크를 찾을 수 없습니다.", request.taskId()));

        if (request.content() == null || request.content().isEmpty()) {
            throw new BadRequestException("내용란이 비어있습니다.", null);
        }

        Comment comment = Comment.builder()
                .task(task)
                .author(user)
                .body(request.content())
                .build();

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toResponse(savedComment);
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findByAuthorIdAndId(userId, commentId)
                .orElseThrow(() -> new NotFoundException("해당 댓글을 찾을 수 없습니다.", commentId));

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new NotFoundException("해당 테스크를 찾을 수 없습니다.", request.taskId()));

        if (task != null) {
            comment.setTask(task);
        }

        if (request.content() != null && !request.content().isEmpty()) {
            comment.setBody(request.content());
        }

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        try {
            commentRepository.deleteByAuthorIdAndId(userId, commentId);
        } catch (RuntimeException e) {
            throw new ConflictException("해당 댓그을 삭제할 수 없습니다.", commentId);
        }
    }
}
