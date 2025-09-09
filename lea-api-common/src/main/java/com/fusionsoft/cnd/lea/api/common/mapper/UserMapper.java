package com.fusionsoft.cnd.lea.api.common.mapper;

import com.fusionsoft.cnd.lea.api.common.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User findById(Long id);
    User findByUserId(String userId);
    boolean existsByUserId(String userId);
    int insertUser(User user);
}
