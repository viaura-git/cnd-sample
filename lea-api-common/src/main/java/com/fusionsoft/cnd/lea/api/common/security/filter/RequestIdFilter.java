package com.fusionsoft.cnd.lea.api.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestId = request.getHeader("x-request-id");
            if (requestId != null) {
                MDC.put("requestId", requestId); // 로그용
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}
