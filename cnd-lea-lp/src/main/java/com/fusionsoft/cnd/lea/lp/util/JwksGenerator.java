package com.fusionsoft.cnd.lea.lp.util;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.KeyUse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwksGenerator {
    public static void main(String[] args) throws Exception {

//        String pem = Files.readString(Path.of("src/main/resources/keys/public.pem"))
//                .replaceAll("-----\\w+ PUBLIC KEY-----", "")
//
        InputStream is = JwksGenerator.class.getClassLoader().getResourceAsStream("keys/public.pem");
        if (is == null) throw new RuntimeException("public.pem not found");
        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(pem);

        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));

        RSAKey key = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .keyID("cnd-dev-key-1")
                .build();

        System.out.println(key.toPublicJWK().toJSONString());
    }

}

