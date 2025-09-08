package com.fusionsoft.cnd.lea.lp.service;


import com.fusionsoft.cnd.lea.lp.domain.dto.LoginRequest;
import com.fusionsoft.cnd.lea.lp.domain.dto.RegisterRequest;
import com.fusionsoft.cnd.lea.lp.domain.entity.Role;
import com.fusionsoft.cnd.lea.lp.domain.entity.User;
import com.fusionsoft.cnd.lea.lp.domain.type.RoleType;
import com.fusionsoft.cnd.lea.lp.provider.JwtProvider;
import com.fusionsoft.cnd.lea.lp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Value("${jwt.refresh-token-validity-seconds}")
    private Long refreshSeconds;

    //Spring Security의 username은 한국의 userId 이다. 즉, "사용자로부터 입력받은 식별자"를 의미한다
    public User register(RegisterRequest req) {

        if (userRepository.existsByUserId(req.userId())) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = User.from(req, passwordEncoder);
        user.setRoles(
                new HashSet<>(Set.of(new Role(RoleType.ROLE_USER)))
                );
        return userRepository.save(user);
    }

    //username은 SpringSecurity 에서 "사용자를 구분하는 식별자" 의미이다. 구현할때 userId, email 등등 자유롭게 바꾼다
    public Map<String, String> login(LoginRequest req) {

        // 1. DB에서 사용자 조회
        User user = userRepository.findByUserId(req.userId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        // 3. 사용자 권한 가져오기
        Set<Role> roles = user.getRoles(); // User 엔티티에 roles 포함

        String accessToken = jwtProvider.generateAccessToken(req.userId(), roles);
        String refreshToken = tokenService.generateAndStoreRefreshToken(req.userId());

        // save refresh token - RDB table 저장용. 이제 redis사용으로 하기는 주석
        /**
         * String refreshTokenStr = jwtProvider.generateRefreshToken(req.userId());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .expiryDate(Instant.now().plusSeconds(refreshSeconds))
                .user(userRepository.findByUserId(req.userId()).orElseThrow())
                .build();

        refreshTokenRepository.save(refreshToken);
         **/


        Map<String,String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public String refreshAccessToken(String refreshToken) {
        // 토큰에서 username 추출
        String username = jwtProvider.parseClaims(refreshToken).getSubject();

        // Redis에 있는 refreshToken 검증
        if (!tokenService.validateRefreshToken(username, refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // User 엔티티 조회
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Access Token 발급
        return jwtProvider.generateAccessToken(user.getUserId(), user.getRoles());
    }


    public void logout(String accessToken) {
        log.debug("AuthService logout start");
        String username = jwtProvider.parseClaims(accessToken).getSubject();
        log.debug("username: {}", username);

        // 1. 토큰 유효성 검증
        jwtProvider.validateToken(accessToken); // 유효하지 않으면 예외 발생

        // 2. JTI 추출
        String jti = jwtProvider.getJti(accessToken);

        // 3. 만료시간 계산
        long expiresIn = jwtProvider.getExpiresInSeconds(accessToken);

        // 4. 블랙리스트 등록
        tokenService.blacklistAccessToken(jti, expiresIn);

        // 5. refreshToken 삭제(선택)
        tokenService.deleteRefreshToken(jwtProvider.parseClaims(accessToken).getSubject());

    }
}

