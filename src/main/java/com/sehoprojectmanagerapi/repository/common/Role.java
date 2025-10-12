package com.sehoprojectmanagerapi.repository.common;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
    // Workspace-level
    WS_OWNER, WS_ADMIN, WS_MEMBER, WS_VIEWER,
    // Space-level
    SP_ADMIN, SP_MEMBER, SP_VIEWER,
    // Project-level
    PR_ADMIN, PR_MEMBER, PR_VIEWER;

    public static Set<Role> rolesGrantingSpaceVisibility() {
        return EnumSet.of(WS_OWNER, WS_ADMIN, WS_MEMBER, WS_VIEWER);
    }

    public static Set<Role> rolesGrantingProjectVisibility() {
        return EnumSet.of(WS_OWNER, WS_ADMIN, SP_ADMIN, SP_MEMBER, SP_VIEWER);
    }
}
