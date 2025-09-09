package com.fusionsoft.cnd.lea.api.common.domain.entity;

import com.fusionsoft.cnd.lea.api.common.domain.type.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Role implements Serializable {

    private RoleType roleName;

    public Role(RoleType roleName) {
        this.roleName = roleName;
    }

}