package com.fusionsoft.cnd.lea.lp.config;

import com.fusionsoft.cnd.lea.lp.filter.JwtBlacklistFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final JwtBlacklistFilter jwtBlacklistFilter;

    public FilterConfig(JwtBlacklistFilter jwtBlacklistFilter) {
        this.jwtBlacklistFilter = jwtBlacklistFilter;
    }

    @Bean
    public FilterRegistrationBean<JwtBlacklistFilter> jwtBlacklistFilterRegistration() {
        FilterRegistrationBean<JwtBlacklistFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtBlacklistFilter);
        registration.addUrlPatterns("/api/*"); // 보호할 엔드포인트
        registration.setOrder(1); // 순서 조정 가능
        return registration;
    }
}
