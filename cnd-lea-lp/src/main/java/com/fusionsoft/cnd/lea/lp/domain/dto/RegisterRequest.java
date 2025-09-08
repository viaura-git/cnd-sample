package com.fusionsoft.cnd.lea.lp.domain.dto;

public record RegisterRequest(
        String userId, String userName, String password, String phone, String email
) {
}
