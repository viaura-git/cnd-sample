package com.fusionsoft.cnd.lea.lp.domain.dto;

import java.util.List;

public record UserInfoResponse(
        String userId,
        String username,
        String email,
        String phone,
        List<String> roles
) {}
