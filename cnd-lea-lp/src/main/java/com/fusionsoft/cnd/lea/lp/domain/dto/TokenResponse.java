package com.fusionsoft.cnd.lea.lp.domain.dto;


import com.fusionsoft.cnd.lea.lp.domain.type.AuthType;

// TokenResponse.java
public record TokenResponse(String accessToken, String refreshToken, AuthType authType) {}

