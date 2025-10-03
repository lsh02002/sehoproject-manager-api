package com.sehoprojectmanagerapi.web.controller.comment;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.comment.CommentService;
import com.sehoprojectmanagerapi.web.dto.comment.CommentRequest;
import com.sehoprojectmanagerapi.web.dto.comment.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAllCommentsByUserId(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return ResponseEntity.ok(commentService.getCommentsByUserId(customUserDetails.getId()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<List<CommentResponse>> getAllCommentsByTaskId(@PathVariable Long taskId){
        return ResponseEntity.ok(commentService.getCommentByTaskId(taskId));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId){
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentService.createComment(customUserDetails.getId(), commentRequest));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("commentId") Long commentId, @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentService.updateComment(customUserDetails.getId(), commentId, commentRequest));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(customUserDetails.getId(), commentId);
    }
}
