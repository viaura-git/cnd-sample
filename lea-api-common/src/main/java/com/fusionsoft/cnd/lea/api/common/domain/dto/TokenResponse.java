package com.fusionsoft.cnd.lea.api.common.domain.dto;


import com.fusionsoft.cnd.lea.api.common.domain.type.AuthType;

// TokenResponse.java
public record TokenResponse(String accessToken, String refreshToken, AuthType authType) {}

