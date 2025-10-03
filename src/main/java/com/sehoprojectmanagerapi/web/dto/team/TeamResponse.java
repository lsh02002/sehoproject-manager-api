package com.sehoprojectmanagerapi.web.dto.team;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TeamResponse {
    private Long id;
    private String name;
}
