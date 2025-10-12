package com.sehoprojectmanagerapi.config.security;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    /**
     * 현재 인증된 사용자의 ID 반환
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증된 사용자가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        // principal 이 UserDetails 또는 커스텀 UserPrincipal 인 경우
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }

        // principal 이 그냥 String (예: username) 인 경우
        if (principal instanceof String username) {
            // 필요 시 username → userId 매핑 로직 추가
            throw new IllegalStateException("userId를 직접 추출할 수 없습니다: " + username);
        }

        throw new IllegalStateException("지원하지 않는 principal 타입: " + principal.getClass());
    }
}
