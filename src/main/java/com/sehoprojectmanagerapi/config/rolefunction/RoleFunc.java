package com.sehoprojectmanagerapi.config.rolefunction;

import com.sehoprojectmanagerapi.repository.project.projectmember.RoleProject;
import com.sehoprojectmanagerapi.repository.team.teammember.RoleTeam;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import org.springframework.stereotype.Component;

@Component
public class RoleFunc {
    public void requireAtLeast(RoleProject actual, RoleProject required, String msg, Object ctx) {
        if (rank(actual) > rank(required)) {
            throw new NotAcceptableException(msg, ctx);
        }
    }

    /**
     * 팀 역할 등급 비교 유틸 (enum 순서 또는 별도 우선순위 맵 기준으로 구현)
     */
    public boolean hasAtLeast(RoleTeam actual, RoleTeam required) {
        // 예시: OWNER > MANAGER > MEMBER > VIEWER
        int rankActual = rank(actual);
        int rankRequired = rank(required);
        return rankActual <= rankRequired; // 숫자 낮을수록 상위 등급이라고 가정
    }

    public boolean hasAtLeast(RoleProject actual, RoleProject required) {
        // 예시: OWNER > MANAGER > MEMBER > VIEWER
        int rankActual = rank(actual);
        int rankRequired = rank(required);
        return rankActual <= rankRequired; // 숫자 낮을수록 상위 등급이라고 가정
    }

    public int rank(RoleTeam role) {
        return switch (role) {
            case OWNER -> 0;
            case ADMIN -> 1;
            case MEMBER -> 2;
            case VIEWER -> 3;
            default -> 99;
        };
    }

    public int rank(RoleProject role) {
        return switch (role) {
            case MANAGER -> 0;
            case CONTRIBUTOR -> 1;
            case VIEWER -> 2;
            default -> 99;
        };
    }
}
