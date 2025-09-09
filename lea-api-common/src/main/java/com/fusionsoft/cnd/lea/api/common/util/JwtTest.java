package com.fusionsoft.cnd.lea.api.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;

@Slf4j
public class JwtTest {

    public static void main(String[] args) {
        testSecretKey();
    }

    public static void testSecretKey() {
        String secret = "FusionsoftCndProjectSecretKeyPleaseCallFusionSoft+82536097353";
        String encodedSecret = Encoders.BASE64.encode(secret.getBytes());
        log.info("encodedSecret : {}", encodedSecret);

        SecretKey randomKey = Jwts.SIG.HS256.key().build();
        log.info("generated SecretKey : {}", randomKey.toString());
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("키 길이가 256비트 이상이어야 합니다");
        }

        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
    }
}
