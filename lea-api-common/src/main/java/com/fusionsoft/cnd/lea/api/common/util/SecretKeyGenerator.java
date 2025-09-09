package com.fusionsoft.cnd.lea.api.common.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class SecretKeyGenerator {

    public static void main(String[] args) {

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Encoded: " + encoded);
        String publicKey = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(key.getEncoded());
        System.out.println("Public Key: " + publicKey);
    }
}
