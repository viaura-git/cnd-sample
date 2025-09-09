package com.fusionsoft.cnd.lea.api.common.security.service;

import com.fusionsoft.cnd.lea.api.common.mapper.UserMapper;
import com.fusionsoft.cnd.lea.api.common.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    //spring security에서 말하는 username은 한국에서 말하는 userId이다
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return Optional.ofNullable(userMapper.findByUserId(username))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 없습니다"));
    }
}

