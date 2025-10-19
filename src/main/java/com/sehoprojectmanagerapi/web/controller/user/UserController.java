package com.sehoprojectmanagerapi.web.controller.user;

import com.sehoprojectmanagerapi.repository.user.userdetails.CustomUserDetails;
import com.sehoprojectmanagerapi.service.exceptions.AccessDeniedException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.user.UserService;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeRequest;
import com.sehoprojectmanagerapi.web.dto.user.LoginRequest;
import com.sehoprojectmanagerapi.web.dto.user.SignupRequest;
import com.sehoprojectmanagerapi.web.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(userService.signUp(signupRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        List<Object> accessTokenAndRefreshTokenAndResponse = userService.login(loginRequest, httpServletRequest);
        httpServletResponse.addHeader("accessToken", accessTokenAndRefreshTokenAndResponse.get(0).toString());
        httpServletResponse.addHeader("refreshToken", accessTokenAndRefreshTokenAndResponse.get(1).toString());

        return ResponseEntity.ok((UserResponse) accessTokenAndRefreshTokenAndResponse.get(2));
    }

    @PostMapping("/logout")
    public ResponseEntity<UserResponse> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails, HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(userService.logout(Objects.requireNonNull(customUserDetails).getEmail(), request, response));
    }

    @GetMapping
    public ResponseEntity<List<AssigneeRequest>> getAllUsers(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(userService.getUserInfos(customUserDetails.getId()));
    }

    @GetMapping(value = "/entrypoint")
    public void entrypointException(@RequestParam(name = "accessToken", required = false) String token) {
        if (token == null) throw new NotAcceptableException("로그인(Jwt 토큰)이 필요합니다.", null);
        else throw new NotAcceptableException("로그인(Jwt 토큰)이 만료 되었습니다. 다시 로그인 하세요", null);
    }

    @GetMapping(value = "/access-denied")
    public void accessDeniedException(@RequestParam(name = "roles", required = false) String roles) {
        if (roles == null) throw new AccessDeniedException("권한이 설정되지 않았습니다.", null);
        else throw new AccessDeniedException("권한이 없습니다.", "시도한 유저의 권한 : " + roles);
    }

    @GetMapping("/test1")
    public ResponseEntity<Object> test1(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(customUserDetails.toString());
    }

    @GetMapping("/test2")
    public ResponseEntity<String> test2() {
        return ResponseEntity.ok("Jwt 토큰이 상관없는 EntryPoint 테스트입니다.");
    }
}
