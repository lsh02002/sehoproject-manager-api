package com.sehoprojectmanagerapi.web.dto.workspace.tree;

public record WorkspaceTreeRow(
        Long wsId, String wsName,
        Long spId, String spName,
        Long prId, String prName
) {}
