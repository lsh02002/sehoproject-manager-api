package com.sehoprojectmanagerapi.web.controller.tag;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.tag.TagService;
import com.sehoprojectmanagerapi.web.dto.tag.TagRequest;
import com.sehoprojectmanagerapi.web.dto.tag.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;

    /**
     * 프로젝트 내 태그 목록
     */
    @GetMapping("/projects/{projectId}/tags")
    public ResponseEntity<List<TagResponse>> getAllTagByProject(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId) {
        return ResponseEntity.ok(tagService.getTagsByProject(customUserDetails.getId(), projectId));
    }

    /**
     * 프로젝트 내 태그 생성
     */
    @PostMapping("/projects/{projectId}/tags")
    public ResponseEntity<TagResponse> createTag(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long projectId, @RequestBody TagRequest tagRequest) {
        TagRequest req = new TagRequest(projectId, tagRequest.name(), tagRequest.description());
        return ResponseEntity.ok(tagService.createTag(customUserDetails.getId(), req));
    }

    /**
     * 태그 단건 조회 (원하면 사용)
     */
    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponse> getTag(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long tagId) {
        return ResponseEntity.ok(tagService.getTagByUserIdAndProjectIdAndId(tagId, customUserDetails.getId(), tagId));
    }

    /**
     * 태그 수정
     */
    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long tagId, @RequestBody TagRequest tagRequest) {
        return ResponseEntity.ok(tagService.updateTag(customUserDetails.getId(), tagId, tagRequest));
    }

    /**
     * 태그 삭제
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long tagId) {
        tagService.deleteTag(customUserDetails.getId(), tagId);
        return ResponseEntity.ok().build();
    }
}
