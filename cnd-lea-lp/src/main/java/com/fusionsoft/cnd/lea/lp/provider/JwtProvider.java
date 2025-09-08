package com.fusionsoft.cnd.lea.lp.provider;


import com.fusionsoft.cnd.lea.lp.domain.entity.Role;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private final String issuer;
    private final long accessTokenValiditySeconds; // 14일
    private final long refreshTokenValiditySeconds; // 30일

    public JwtProvider(@Value("${JWT_ISSUER:cnd-dev}") String issuer,
                       @Value("${JWT_JWT_ACCESS_TOKEN_VALIDITY:1209600}") long accessTokenValiditySeconds,
                       @Value("${JWT_REFRESH_TOKEN_VALIDITY:2592000}") long refreshTokenValiditySeconds) throws Exception {

        // private/public key 파일에서 로딩
        try {
            this.privateKey = loadPrivateKey("/keys/private.pem");
            this.publicKey = loadPublicKey("/keys/public.pem");
        } catch (Exception e) {
            throw new IllegalStateException("JWT 키 로딩 실패", e);
        }
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        this.issuer = issuer;
    }

    // accessToken 생성
    public String generateAccessToken(String userId, Set<Role> roles) {
        Instant now = Instant.now();

        Set<String> roleNames = roles.stream()
                .map(role -> role.getRoleName().name()) // ROLE_ADMIN, ROLE_USER …
                .collect(Collectors.toSet());

        return Jwts.builder()
                .subject(userId)
                .issuer(issuer)
                .claim("roles", roleNames)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenValiditySeconds)))
                .signWith(privateKey)
                .header().add("kid", "cnd-dev-key-1")
                .and()
                .compact();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenValiditySeconds)))
                .signWith(privateKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰도 Claims 자체는 얻을 수 있음
            return e.getClaims();
        }
    }

    // JTI 추출
    public String getJti(String token) {
        return parseClaims(token).getId();
    }


    public boolean validateToken(String token) {

        log.debug("token validation start");
        try {
            Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);;
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("Wrong Signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }


    // 만료까지 남은 시간
    public long getExpiresInSeconds(String token) {
        Claims claims = Jwts.parser().verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        long gapMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(1, gapMillis / 1000);
    }



    private PrivateKey loadPrivateKey(String path) throws Exception {

        Resource resource = new ClassPathResource(path);
        String key = resource.getContentAsString(StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        String key = resource.getContentAsString(StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}


