package com.fusionsoft.cnd.lea.api.common.security.service;

import com.fusionsoft.cnd.lea.api.common.security.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    public String generateAndStoreRefreshToken(String username) {
        String refreshToken = jwtProvider.generateRefreshToken(username);
        String key = "refresh:" + username;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofSeconds(jwtProvider.getExpiresInSeconds(refreshToken))
        );
        return refreshToken;
    }

    public boolean validateRefreshToken(String username, String refreshToken) {
        String key = "refresh:" + username;
        String stored = redisTemplate.opsForValue().get(key);
        return stored != null && stored.equals(refreshToken) && jwtProvider.validateToken(refreshToken);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete("refresh:" + username);
    }

    public void blacklistAccessToken(String jti, long expiresInSeconds) {
        String key = "blacklist:" + jti;
        redisTemplate.opsForValue().set(key, "true", Duration.ofSeconds(expiresInSeconds));
    }

    public boolean isBlacklisted(String jti) {
        String key = "blacklist:" + jti;
        return redisTemplate.hasKey(key);
    }


}

