package com.fusionsoft.cnd.lea.api.common.controller;

import com.fusionsoft.cnd.lea.api.common.domain.dto.*;
import com.fusionsoft.cnd.lea.api.common.domain.type.AuthType;
import com.fusionsoft.cnd.lea.api.common.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//HomeController 는 logout만 token 체크를 한다
@Slf4j
@RestController
@RequestMapping("/api/v1/lea")
@RequiredArgsConstructor
public class HomeController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        log.info("===> start login");

        var tokens = authService.login(req);
        log.debug("=====> tokens: {}", tokens);

        TokenResponse tokenResponse = new TokenResponse(
                tokens.get("accessToken"),
                tokens.get("refreshToken"),
                AuthType.BEARER
        );

        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRequest request){
        log.debug("new access token publish start : /refresh");

        String refreshToken = request.refreshToken();
        log.debug("refreshToken: {}", refreshToken);

        String newAccessToken = authService.refreshAccessToken(refreshToken);
        log.debug("newAccessToken: {}", newAccessToken);

        TokenResponse tokenResponse = new TokenResponse(newAccessToken, null, AuthType.BEARER);

        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        log.debug("logout request Authorization:{}", request.getHeader("Authorization"));
        String username = request.getHeader("x-cnd-username");

        if (!StringUtils.hasText(username)) {
            // 헤더 없음 → 인증 필요
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("AUTH-001", "Authorization header missing"));
        }

//        if (!header.startsWith("Bearer ")) {
//            // 형식 오류
//            return ResponseEntity.badRequest()
//                    .body(ApiResponse.error("AUTH-002", "Invalid Authorization header format"));
//        }
//
//        String accessToken = header.substring(7);
//        authService.logout(accessToken);

        // 성공 → data는 없음
        return ResponseEntity.ok(ApiResponse.success("logout complete", null));
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){
        var insertCount = authService.register(req);
        return ResponseEntity.ok(insertCount +"행 추가");
    }

}
