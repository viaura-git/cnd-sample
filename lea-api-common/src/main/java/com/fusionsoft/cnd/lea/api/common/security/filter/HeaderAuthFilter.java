package com.fusionsoft.cnd.lea.api.common.security.filter;

import com.fusionsoft.cnd.lea.api.common.domain.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Collections.list(request.getHeaderNames())
                .forEach(name -> log.debug("Header: {}={}", name, request.getHeader(name)));

        String username = request.getHeader("x-cnd-username");
        log.debug("=====> username from Header: {}", username);
        String roles = request.getHeader("X-cnd-roles");
        log.debug("=====> rolesHeader from Header: {}", roles);

        if (username != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (roles != null && !roles.isBlank()) {
                for (String role : roles.split(",")) {
                    authorities.add(new SimpleGrantedAuthority(role.trim()));
                }
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            log.debug("x-cnd-username header is null");
            sendUnauthorized(response, "Authorization header missing");

            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Object> apiResponse = ApiResponse.error("AUTH-001", message);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(apiResponse);

        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
