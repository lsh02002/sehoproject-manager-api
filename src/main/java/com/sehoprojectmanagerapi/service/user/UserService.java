package com.sehoprojectmanagerapi.service.user;

import com.sehoprojectmanagerapi.config.function.SnapshotFunc;
import com.sehoprojectmanagerapi.config.redis.RedisUtil;
import com.sehoprojectmanagerapi.config.security.JwtTokenProvider;
import com.sehoprojectmanagerapi.repository.activity.ActivityAction;
import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.ProjectRepository;
import com.sehoprojectmanagerapi.repository.space.Space;
import com.sehoprojectmanagerapi.repository.space.SpaceRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.repository.user.refreshToken.RefreshToken;
import com.sehoprojectmanagerapi.repository.user.refreshToken.RefreshTokenRepository;
import com.sehoprojectmanagerapi.repository.workspace.Workspace;
import com.sehoprojectmanagerapi.repository.workspace.WorkspaceRepository;
import com.sehoprojectmanagerapi.service.activitylog.ActivityLogService;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.task.AssigneeRequest;
import com.sehoprojectmanagerapi.web.dto.user.LoginRequest;
import com.sehoprojectmanagerapi.web.dto.user.SignupRequest;
import com.sehoprojectmanagerapi.web.dto.user.SignupResponse;
import com.sehoprojectmanagerapi.web.dto.user.UserResponse;
import com.sehoprojectmanagerapi.web.mapper.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final UserMapper userMapper;
    private final ActivityLogService activityLogService;
    private final SnapshotFunc snapshotFunc;

    private final SpaceRepository spaceRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public UserResponse signUp(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        if (!email.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")) {
            throw new BadRequestException("이메일을 정확히 입력해주세요.", email);
        }

        if (signupRequest.getNickname().matches("01\\d{9}")) {
            throw new BadRequestException("전화번호를 이름으로 사용할수 없습니다.", signupRequest.getNickname());
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("이미 입력하신 " + email + " 이메일로 가입된 계정이 있습니다.", email);
        }

        if (userRepository.existsByNickname(signupRequest.getNickname())) {
           throw new ConflictException("이미 입력하신 " + signupRequest.getNickname() + " 이메일로 가입된 계정이 있습니다.", signupRequest.getNickname());
        }

        if (signupRequest.getNickname().trim().isEmpty() || signupRequest.getNickname().length() > 30) {
            throw new BadRequestException("이름은 비어있지 않고 30자리 이하여야 합니다.", signupRequest.getNickname());
        }

        if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]+$")
                || !(password.length() >= 8 && password.length() <= 20)) {
            throw new BadRequestException("비밀번호는 8자 이상 20자 이하 숫자와 영문소문자 조합 이어야 합니다.", password);
        }

        if (!signupRequest.getPasswordConfirm().equals(password)) {
            throw new BadRequestException("비밀번호와 비밀번호 확인이 같지 않습니다.", "password : " + password + ", password_confirm : " + signupRequest.getPasswordConfirm());
        }

        signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        User user = User.builder()
                .email(signupRequest.getEmail())
                .passwordHash(signupRequest.getPassword())
                .nickname(signupRequest.getNickname())
                .timezone(signupRequest.getTimezone())
                .userStatus("정상")
                .isActive(true)
                .build();

        userRepository.save(user);

        Object afteruser = snapshotFunc.snapshot(user);

        SignupResponse signupResponse = SignupResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .workspaceId(user.getWorkspaceId())
                .spaceId(user.getSpaceId())
                .projectId(user.getProjectId())
                .build();

        activityLogService.log(ActivityEntityType.USER, ActivityAction.CREATE, user.getId(), user.logMessage(), user, null, afteruser);

        return new UserResponse(HttpStatus.OK.value(), user.getNickname() + "님 회원 가입 완료 되었습니다.", signupResponse);
    }

    @Transactional
    public List<Object> login(LoginRequest request, HttpServletRequest httpServletRequest) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new BadRequestException("이메일이나 비밀번호 값이 비어있습니다.", "email : " + request.getEmail() + ", password : " + request.getPassword());
        }
        User user;

        if (request.getEmail().matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")) {
            user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new NotFoundException("입력하신 이메일의 계정을 찾을 수 없습니다.", request.getEmail()));
        } else {
            throw new BadRequestException("이메일이나 비밀번호가 잘못 입력되었습니다.", null);
        }
        String p1 = user.getPasswordHash();

        if (!passwordEncoder.matches(request.getPassword(), p1)) {
            throw new BadRequestException("이메일이나 비밀번호가 잘못 입력되었습니다.", null);
        }

        SignupResponse signupResponse = SignupResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .workspaceId(user.getWorkspaceId())
                .spaceId(user.getSpaceId())
                .projectId(user.getProjectId())
                .build();

        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        RefreshToken newToken = RefreshToken.builder()
                .authId(user.getId().toString())
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .build();

        refreshTokenRepository.save(newToken);

        UserResponse authResponse = new UserResponse(HttpStatus.OK.value(), "로그인에 성공 하였습니다.", signupResponse);

        return Arrays.asList(jwtTokenProvider.createAccessToken(user.getEmail()), newRefreshToken, authResponse);
    }

    @Transactional
    public UserResponse logout(String email, HttpServletRequest request, HttpServletResponse response) {
        String accessToken = request.getHeader("accessToken");

        if (email == null) {
            throw new BadRequestException("유저 정보가 비어있습니다.", null);
        }

        RefreshToken deletedToken = refreshTokenRepository.findByEmail(email);
        if (deletedToken != null) {
            refreshTokenRepository.delete(deletedToken);
        }

        if (jwtTokenProvider.validateToken(accessToken)) {
            redisUtil.setBlackList(accessToken, "accessToken", 30);
        }

        return new UserResponse(HttpStatus.OK.value(), "로그아웃에 성공 하였습니다.", null);
    }

    @Transactional
    public UserResponse withdrawal(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("계정을 찾을 수 없습니다. 다시 로그인 해주세요.", email));

        Object beforeUser = snapshotFunc.snapshot(user);

        if (user.getUserStatus().equals("탈퇴")) {
            throw new BadRequestException("이미 탈퇴처리된 회원 입니다.", email);
        }
        user.setUserStatus("탈퇴");
        user.setDeletedAt(LocalDateTime.now());

        Object afterUser = snapshotFunc.snapshot(user);

        activityLogService.log(ActivityEntityType.USER, ActivityAction.DELETE, user.getId(), user.logMessage(), user, beforeUser, afterUser);

        return new UserResponse(200, "회원탈퇴 완료 되었습니다.", user.getNickname());
    }

    @Transactional
    public SignupResponse getUserByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", null));

        return SignupResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .workspaceId(user.getWorkspaceId())
                .spaceId(user.getSpaceId())
                .projectId(user.getProjectId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public List<AssigneeRequest> getUserInfos(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", null));

        return userRepository.findAll()
                .stream().map(userMapper::toAssigneeRequest).toList();
    }

    @Transactional
    public Long setUserProjectId(Long userId, Long workspaceId, Long spaceId, Long projectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다.", userId));

        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("해당 워크스페이스를 찾을 수 없습니다.", workspaceId));

        if(Objects.equals(user.getWorkspaceId(), workspaceId) && Objects.equals(user.getSpaceId(), spaceId) && Objects.equals(user.getProjectId(), projectId)) {
            throw new NotAcceptableException("변경된 사항이 없습니다.", null);
        }

        Long targetSpaceId;
        Long targetProjectId;

        // 1. space 결정
        if (spaceRepository.existsByIdAndWorkspaceId(spaceId, workspaceId)) {
            targetSpaceId = spaceId;
        } else {
            List<Space> fallbackSpaces = spaceRepository.findFirstByWorkspaceIdOrderByIdAsc(workspaceId);

            if(fallbackSpaces.isEmpty()) {
                throw  new NotFoundException("해당 워크스페이스에 스페이스가 없습니다.", workspaceId);
            }

            targetSpaceId = fallbackSpaces.get(0).getId();
        }

        // 2. project 결정
        if (projectRepository.existsByIdAndSpaceId(projectId, targetSpaceId)) {
            targetProjectId = projectId;
        } else {
            List<Project> fallbackProjects = projectRepository.findFirstBySpaceIdOrderByIdAsc(targetSpaceId);

            if(fallbackProjects.isEmpty()) {
                throw new NotFoundException("해당 스페이스에 프로젝트가 없습니다.", targetSpaceId);
            }
            targetProjectId = fallbackProjects.get(0).getId();
        }

        // 3. user에 반영
        user.setWorkspaceId(workspaceId);
        user.setSpaceId(targetSpaceId);
        user.setProjectId(targetProjectId);

        return user.getProjectId();
    }
}
