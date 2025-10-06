package com.sehoprojectmanagerapi.web.controller.sample;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.sample.SampleService;
import com.sehoprojectmanagerapi.web.dto.workspace.WorkspaceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/samples")
public class SampleController {

    private final SampleService sampleService;

    @PostMapping
    public ResponseEntity<Void> createSample(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody WorkspaceRequest workspaceRequest) {
        sampleService.createSample(customUserDetails.getId(), workspaceRequest);
        return ResponseEntity.ok().build();
    }
}
