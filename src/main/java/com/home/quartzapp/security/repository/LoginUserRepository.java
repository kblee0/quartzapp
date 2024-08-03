package com.home.quartzapp.security.repository;

import com.home.quartzapp.security.entity.LoginUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface LoginUserRepository {
    Optional<LoginUser> getLoginUser(@Param("loginId") String loginId);
    int updateLoginUserRefreshToken(@Param("loginId") String loginId, @Param("refreshToken") String refreshToken);
}
