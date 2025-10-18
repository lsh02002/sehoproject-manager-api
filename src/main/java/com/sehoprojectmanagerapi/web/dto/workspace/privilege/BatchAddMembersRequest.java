package com.sehoprojectmanagerapi.web.dto.workspace.privilege;

import java.util.List;

public record BatchAddMembersRequest(
        String email,
        String requestRole, // "ADMIN" | "MEMBER" | "VIEWER"
        String roleProject,
        String note,
        List<Long> spaceIds,
        List<Long> projectIds
) {}
