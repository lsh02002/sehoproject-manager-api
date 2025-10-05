package com.sehoprojectmanagerapi.repository.common;

import lombok.Getter;

@Getter
public enum MemberStatus {

    INVITED("초대됨"),
    ACTIVE("활성"),
    INACTIVE("비활성"),
    REMOVED("삭제됨");

    private final String description;

    MemberStatus(String description) {
        this.description = description;
    }
}
