package com.fusionsoft.cnd.lea.api.common.domain.dto;

public record RegisterRequest(
        String userId, String userName, String password, String phone, String email
) {
}
