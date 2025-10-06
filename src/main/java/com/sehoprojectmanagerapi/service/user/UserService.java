package com.sehoprojectmanagerapi.service.user;

import com.sehoprojectmanagerapi.config.redis.RedisUtil;
import com.sehoprojectmanagerapi.config.security.JwtTokenProvider;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.repository.user.refreshToken.RefreshToken;
import com.sehoprojectmanagerapi.repository.user.refreshToken.RefreshTokenRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.user.LoginRequest;
import com.sehoprojectmanagerapi.web.dto.user.SignupRequest;
import com.sehoprojectmanagerapi.web.dto.user.SignupResponse;
import com.sehoprojectmanagerapi.web.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @Transactional
    public UserResponse signUp(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        if (!email.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")) {
            throw new BadRequestException("이메일을 정확히 입력해주세요.", email);
        } else if (signupRequest.getName().matches("01\\d{9}")) {
            throw new BadRequestException("전화번호를 이름으로 사용할수 없습니다.", signupRequest.getName());
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("이미 입력하신 " + email + " 이메일로 가입된 계정이 있습니다.", email);
        } else if (signupRequest.getName().trim().isEmpty() || signupRequest.getName().length() > 30) {
            throw new BadRequestException("이름은 비어있지 않고 30자리 이하여야 합니다.", signupRequest.getName());
        } else if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]+$")
                || !(password.length() >= 8 && password.length() <= 20)) {
            throw new BadRequestException("비밀번호는 8자 이상 20자 이하 숫자와 영문소문자 조합 이어야 합니다.", password);
        } else if (!signupRequest.getPasswordConfirm().equals(password)) {
            throw new BadRequestException("비밀번호와 비밀번호 확인이 같지 않습니다.", "password : " + password + ", password_confirm : " + signupRequest.getPasswordConfirm());
        }

        signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        User user = User.builder()
                .email(signupRequest.getEmail())
                .passwordHash(signupRequest.getPassword())
                .name(signupRequest.getName())
                .timezone(signupRequest.getTimezone())
                .isActive(true)
                .build();

        userRepository.save(user);

        SignupResponse signupResponse = SignupResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .build();

        return new UserResponse(HttpStatus.OK.value(), user.getName() + "님 회원 가입 완료 되었습니다.", signupResponse);
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
                .name(user.getName())
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
}
