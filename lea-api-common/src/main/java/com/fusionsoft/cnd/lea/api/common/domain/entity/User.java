package com.fusionsoft.cnd.lea.api.common.domain.entity;

import com.fusionsoft.cnd.lea.api.common.domain.dto.RegisterRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter @Setter @ToString(exclude = {"password", "phone", "regDate", "updDate"})
@Builder
@AllArgsConstructor @NoArgsConstructor
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 8644532104784328711L;

    private Long uId;

    @NotNull //validation not null 의미
    private String userId;

    @NotNull
    private String userName;

    @Size(max = 255)
    @NotNull
    private String password;

    @Size(max = 50) //validation 용 사이즈 제한
    @NotNull
    private String email;

    @Size(max = 20)
    @NotNull
    private String phone;

    @Builder.Default
    private Boolean enabled = true;

    @CreatedDate
    private LocalDateTime regDate;

    @LastModifiedDate
    private LocalDateTime updDate;

    private Set<Role> roles = new HashSet<>();

    public static User from(RegisterRequest req, PasswordEncoder passwordEncoder) {
        return User.builder()
                .userId(req.userId())
                .password(passwordEncoder.encode(req.password()))
                .userName(req.userName())
                .email(req.email())
                .phone(req.phone())
                .build();
    }

}
