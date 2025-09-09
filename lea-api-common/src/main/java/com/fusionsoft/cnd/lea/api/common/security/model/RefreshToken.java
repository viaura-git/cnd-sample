package com.fusionsoft.cnd.lea.api.common.security.model;

import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
//@RequiredArgsConstructor //final, lombok @NonNull인 것들만 생성한다. @RequiredArgsConstructor가 lombok것이기 때문
//하지만, Builder패턴이 더 유연하기에 Builder를 쓴다
@AllArgsConstructor
@Builder
public class RefreshToken {

    private Long rId;

    @NonNull
    private String token;

    @NonNull
    private Instant expiryDate;
}

