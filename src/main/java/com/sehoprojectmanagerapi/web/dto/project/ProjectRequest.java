package com.sehoprojectmanagerapi.web.dto.project;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProjectRequest {
    private Long spaceId;
    private String projectKey;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Long creatorId;
    private List<String> tags;
}
