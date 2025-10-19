package com.sehoprojectmanagerapi.web.dto.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssigneeRequest {
    private Long assigneeId;
    private String email;
    private Boolean dynamicAssign;
    private String type;
}
