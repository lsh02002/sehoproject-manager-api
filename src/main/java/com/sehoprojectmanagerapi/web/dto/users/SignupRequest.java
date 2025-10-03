package com.sehoprojectmanagerapi.web.dto.users;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SignupRequest {
    private String email;
    private String password;
    private String passwordConfirm;
    private String name;
    private String timezone;
}
