package com.sehoprojectmanagerapi.web.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record UserResponse(
        int code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object data
) {
}
