package com.sehoprojectmanagerapi.web.dto.workspace.privilege;

public record MemberResponse(Long id, Long userId, Long containerId, String containerType, String requestRole, String roleProject) {}
