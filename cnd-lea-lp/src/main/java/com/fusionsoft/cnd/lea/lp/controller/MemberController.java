package com.fusionsoft.cnd.lea.lp.controller;

import com.fusionsoft.cnd.lea.lp.domain.dto.ApiResponse;
import com.fusionsoft.cnd.lea.lp.domain.dto.UserInfoResponse;
import com.fusionsoft.cnd.lea.lp.domain.entity.User;
import com.fusionsoft.cnd.lea.lp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestHeader("x-cnd-username") String username,
                                       @RequestHeader("x-cnd-roles") String userRoles) {
        log.debug("=====> getMyInfo() start");

        log.debug("=====> user id : {}", username);
        log.debug("=====> user roles : {}", userRoles);

        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 사용자입니다"));

        log.debug("Myinfo from RDB is {}", user);

        UserInfoResponse response = new UserInfoResponse(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles().stream()
                        .map(r -> r.getRoleName().name())
                        .collect(Collectors.toList())
        );

        // 예시로 username, roles만 반환
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
