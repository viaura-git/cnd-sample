package com.fusionsoft.cnd.lea.api.common.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwksGenerator {

    public static void main(String[] args) throws Exception {
        InputStream is = JwksGenerator.class.getClassLoader()
                .getResourceAsStream("keys/public.pem");
        if (is == null) throw new RuntimeException("public.pem not found");

        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(pem);

        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));

        // 수작업으로 JWKS JSON 생성
        String jwksJson = String.format(
                "{\n" +
                        "  \"keys\": [\n" +
                        "    {\n" +
                        "      \"kty\": \"RSA\",\n" +
                        "      \"use\": \"sig\",\n" +
                        "      \"alg\": \"RS256\",\n" +
                        "      \"kid\": \"cnd-dev-key-1\",\n" +
                        "      \"n\": \"%s\",\n" +
                        "      \"e\": \"%s\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}",
                Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()),
                Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray())
        );

        System.out.println(jwksJson);
    }
}


