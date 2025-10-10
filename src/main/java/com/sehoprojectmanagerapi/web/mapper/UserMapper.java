package com.sehoprojectmanagerapi.web.mapper;

import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeRequest;
import com.sehoprojectmanagerapi.web.dto.user.UserInfoResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserInfoResponse toResponse(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(String.valueOf(user.getCreatedAt()))
                .deletedAt(String.valueOf(user.getDeletedAt()))
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
