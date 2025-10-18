package com.sehoprojectmanagerapi.web.dto.workspace.privilege;

public record AddMemberRequest(
        String email,
        String requestRole, // "ADMIN" | "MEMBER" | "VIEWER"
        String roleProject,
        String note
) {}
