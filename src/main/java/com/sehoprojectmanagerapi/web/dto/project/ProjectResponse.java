package com.sehoprojectmanagerapi.web.dto.project;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProjectResponse {
    private Long id;
    private Long spaceId;
    private String spaceName;
    private String projectKey;
    private String name;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Long creatorId;
    private String creatorName;
}
