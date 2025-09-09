package com.fusionsoft.cnd.lea.lp.filter;

import com.fusionsoft.cnd.lea.lp.provider.JwtProvider;
import com.fusionsoft.cnd.lea.lp.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Collections.list(request.getHeaderNames())
                .forEach(name -> log.debug("Header: {}={}", name, request.getHeader(name)));

        String path = request.getRequestURI();

        // 예외 경로: 로그인, 회원가입
        if (path.startsWith("/api/v1/members/login") ||
                path.startsWith("/api/v1/members/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtProvider.parseClaims(token);
                String jti = claims.getId(); // JWT ID

                if (tokenService.isBlacklisted(jti)) {
                    log.debug("Blacklist token detected: {}", jti);

                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("""
                        {"success":false,"code":"AUTH-003","message":"유효한 토큰이 아닙니다","data":null}
                        """);
                    return; // 더 이상 진행 안 함
                }
            } catch (Exception e) {
                log.error("Token parse failed", e);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }

        // 정상 흐름
        filterChain.doFilter(request, response);
    }
}

