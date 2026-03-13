package com.sehoprojectmanagerapi.web.mapper.user;

import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeRequest;
import com.sehoprojectmanagerapi.web.dto.user.UserInfoResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserInfoResponse toResponse(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    public AssigneeRequest toAssigneeRequest(User user) {
        return AssigneeRequest.builder()
                .assigneeId(user.getId())
                .dynamicAssign(false)
                .type("USER")
                .email(user.getEmail())
                .build();
    }
}
