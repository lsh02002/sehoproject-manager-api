package com.sehoprojectmanagerapi.web.dto.project;

import com.sehoprojectmanagerapi.web.dto.team.TeamResponse;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProjectResponse {
    private Long projectId;
    private List<TeamResponse> teams;
    private String projectKey;
    private String name;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String creatorName;
}
